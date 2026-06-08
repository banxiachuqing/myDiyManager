package com.ruoyi.system.ai.llm;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * OpenAI Chat Completions 协议客户端（Hutool 实现）。
 */
public class OpenAiCompatClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatClient.class);

    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final int timeoutSec;

    public OpenAiCompatClient(String baseUrl, String apiKey, String model, int timeoutSec) {
        // 兼容用户填 "https://api.example.com" 或 "https://api.example.com/v1" 两种写法
        // 统一剥掉末尾的 /v1 和斜杠，保证拼接 /v1/models 时不重复
        String normalized = baseUrl == null ? "" : baseUrl.trim();
        normalized = normalized.replaceAll("/v1/?$", "");
        normalized = normalized.replaceAll("/+$", "");
        this.baseUrl = normalized;
        this.apiKey = apiKey;
        this.model = model;
        this.timeoutSec = timeoutSec <= 0 ? 30 : timeoutSec;
    }

    @Override
    public List<String> listModels() {
        HttpResponse resp = HttpRequest.get(baseUrl + "/v1/models")
            .header("Authorization", "Bearer " + apiKey)
            .timeout(timeoutSec * 1000)
            .executeAsync();
        int status = resp.getStatus();
        if (status == 401 || status == 403) {
            throw new LlmException(LlmException.Code.AUTH, "鉴权失败 HTTP " + status);
        }
        if (status == 404) {
            throw new LlmException(LlmException.Code.NETWORK,
                "listModels 返回 404，请检查 baseUrl 是否正确（应填域名，如 https://api.example.com，不要包含 /v1 或 /v1/models）");
        }
        if (!resp.isOk()) {
            throw new LlmException(LlmException.Code.NETWORK, "listModels HTTP " + status);
        }
        JSONObject json;
        try {
            json = JSONUtil.parseObj(resp.body());
        } catch (Exception e) {
            throw new LlmException(LlmException.Code.INVALID_RESPONSE, "解析 listModels 响应失败", e);
        }
        JSONArray data = json.getJSONArray("data");
        List<String> ids = new ArrayList<>();
        if (data != null) {
            for (Object o : data) {
                if (o instanceof JSONObject) ids.add(((JSONObject) o).getStr("id"));
            }
        }
        return ids;
    }

    @Override
    public ChatResponse chat(ChatRequest req) {
        req.setStream(false);
        HttpResponse resp = doPost(req);
        return parseNonStream(resp.body());
    }

    @Override
    public void chatStream(ChatRequest req, Consumer<ChatChunk> handler) {
        req.setStream(true);
        // 关键：必须用 executeAsync()，否则 Hutool 默认把 body 缓存到内存再返回，
        // 配合 resp.bodyStream() 的 readLine 也是从已 buffer 的 byte[] 读，根本不是 socket 流式
        HttpResponse resp = doPost(req);
        try {
            int status = resp.getStatus();
            if (status == 401 || status == 403) {
                throw new LlmException(LlmException.Code.AUTH, "鉴权失败 HTTP " + status);
            }
            if (status == 429) {
                throw new LlmException(LlmException.Code.RATE_LIMIT, "调用频率超限");
            }
            if (!resp.isOk()) {
                throw new LlmException(LlmException.Code.NETWORK, "chatStream HTTP " + status);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resp.bodyStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (StrUtil.isBlank(line)) continue;
                    if (!line.startsWith("data:")) continue;
                    String data = line.substring(5).trim();
                    if ("[DONE]".equals(data)) break;
                    parseAndDispatch(data, handler);
                }
            } catch (java.io.IOException e) {
                throw new LlmException(LlmException.Code.NETWORK, "读取 SSE 流失败", e);
            }
        } finally {
            resp.close();
        }
    }

    private void parseAndDispatch(String data, Consumer<ChatChunk> handler) {
        try {
            JSONObject json = JSONUtil.parseObj(data);
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) return;
            JSONObject choice = choices.getJSONObject(0);
            if (choice == null) return;
            JSONObject delta = choice.getJSONObject("delta");
            ChatChunk chunk = new ChatChunk();
            chunk.setMessageId(json.getStr("id"));
            chunk.setDelta(delta != null ? delta.getStr("content") : null);
            chunk.setFinishReason(choice.getStr("finish_reason"));
            handler.accept(chunk);
        } catch (Exception e) {
            throw new LlmException(LlmException.Code.INVALID_RESPONSE, "解析 SSE 帧失败: " + data, e);
        }
    }

    private HttpResponse doPost(ChatRequest req) {
        return HttpRequest.post(baseUrl + "/v1/chat/completions")
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .timeout(timeoutSec * 1000)
            .body(JSONUtil.toJsonStr(req))
            .executeAsync();
    }

    private void checkStatus(HttpResponse resp) {
        int status = resp.getStatus();
        if (status == 401 || status == 403) {
            throw new LlmException(LlmException.Code.AUTH, "鉴权失败 HTTP " + status);
        }
        if (status == 429) {
            throw new LlmException(LlmException.Code.RATE_LIMIT, "调用频率超限");
        }
        if (!resp.isOk()) {
            throw new LlmException(LlmException.Code.NETWORK, "chat HTTP " + status);
        }
    }

    private ChatResponse parseNonStream(String body) {
        JSONObject json;
        try {
            json = JSONUtil.parseObj(body);
        } catch (Exception e) {
            throw new LlmException(LlmException.Code.INVALID_RESPONSE, "解析 chat 响应失败", e);
        }
        ChatResponse resp = new ChatResponse();
        resp.setId(json.getStr("id"));
        resp.setModel(json.getStr("model"));
        JSONArray choices = json.getJSONArray("choices");
        if (choices != null && !choices.isEmpty()) {
            JSONObject choice0 = choices.getJSONObject(0);
            if (choice0 != null) {
                JSONObject msg = choice0.getJSONObject("message");
                resp.setContent(msg != null ? msg.getStr("content") : null);
            }
        }
        JSONObject usage = json.getJSONObject("usage");
        if (usage != null) resp.setTotalTokens(usage.getInt("total_tokens"));
        return resp;
    }

    public String getModel() { return model; }
    public String getBaseUrl() { return baseUrl; }
}
