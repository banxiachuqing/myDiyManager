package com.ruoyi.system.ai.llm;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiCompatClientTest {

    private HttpServer server;
    private int port;
    private OpenAiCompatClient client;

    @BeforeEach
    void setup() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        port = server.getAddress().getPort();
        // 末尾 /v1 应被自动剥离，最终请求 = /v1/models
        client = new OpenAiCompatClient("http://127.0.0.1:" + port + "/v1", "test-key", "test-model", 5);
    }

    @AfterEach
    void teardown() { server.stop(0); }

    @Test
    void listModels_returnsParsedList() {
        server.createContext("/v1/models", exchange -> {
            String body = "{\"data\":[{\"id\":\"gpt-4\"},{\"id\":\"gpt-3.5\"}]}";
            exchange.sendResponseHeaders(200, body.length());
            exchange.getResponseBody().write(body.getBytes());
            exchange.close();
        });
        List<String> models = client.listModels();
        assertEquals(2, models.size());
        assertTrue(models.contains("gpt-4"));
        assertTrue(models.contains("gpt-3.5"));
    }

    @Test
    void listModels_throwsAuthOn401() {
        server.createContext("/v1/models", exchange -> {
            exchange.sendResponseHeaders(401, 0);
            exchange.close();
        });
        LlmException ex = assertThrows(LlmException.class, () -> client.listModels());
        assertEquals(LlmException.Code.AUTH, ex.getCode());
    }

    @Test
    void listModels_stripsTrailingV1FromBaseUrl() throws IOException {
        // 独立验证：baseUrl 带 /v1 时，最终请求路径必须是 /v1/models，不能是 /v1/v1/models
        java.util.concurrent.atomic.AtomicReference<String> path = new java.util.concurrent.atomic.AtomicReference<>();
        HttpServer s2 = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        s2.createContext("/v1/models", ex -> {
            path.set(ex.getRequestURI().getPath());
            String body = "{\"data\":[]}";
            ex.sendResponseHeaders(200, body.length());
            ex.getResponseBody().write(body.getBytes());
            ex.close();
        });
        s2.start();
        try {
            int p = s2.getAddress().getPort();
            OpenAiCompatClient c = new OpenAiCompatClient(
                "http://127.0.0.1:" + p + "/v1/", "k", "m", 5);
            c.listModels();
            assertEquals("/v1/models", path.get());
        } finally {
            s2.stop(0);
        }
    }

    @Test
    void listModels_throwsNetworkOn500() {
        server.createContext("/v1/models", exchange -> {
            exchange.sendResponseHeaders(500, 0);
            exchange.close();
        });
        LlmException ex = assertThrows(LlmException.class, () -> client.listModels());
        assertEquals(LlmException.Code.NETWORK, ex.getCode());
    }

    @Test
    void chat_returnsParsedContent() {
        server.createContext("/v1/chat/completions", exchange -> {
            String body = "{\"id\":\"x\",\"model\":\"test-model\","
                + "\"choices\":[{\"message\":{\"content\":\"hi\"}}],"
                + "\"usage\":{\"total_tokens\":3}}";
            byte[] bytes = body.getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        ChatRequest req = new ChatRequest();
        req.setModel("test-model");
        req.setMessages(Arrays.asList(msgOf("user", "hi")));
        ChatResponse resp = client.chat(req);
        assertEquals("hi", resp.getContent());
        assertEquals(3, resp.getTotalTokens());
    }

    private static Map<String, String> msgOf(String role, String content) {
        Map<String, String> m = new HashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    @Test
    void chatStream_invokesHandlerForEachChunk() throws Exception {
        server.createContext("/v1/chat/completions", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, 0);
            java.io.OutputStream out = exchange.getResponseBody();
            out.write("data: {\"id\":\"m1\",\"choices\":[{\"delta\":{\"content\":\"he\"},\"finish_reason\":null}]}\n\n".getBytes());
            out.flush();
            out.write("data: {\"id\":\"m1\",\"choices\":[{\"delta\":{\"content\":\"llo\"},\"finish_reason\":\"stop\"}]}\n\n".getBytes());
            out.flush();
            out.write("data: [DONE]\n\n".getBytes());
            out.flush();
            exchange.close();
        });
        ChatRequest req = new ChatRequest();
        req.setModel("test-model");
        req.setMessages(Arrays.asList(msgOf("user", "hi")));
        AtomicInteger count = new AtomicInteger();
        StringBuilder collected = new StringBuilder();
        client.chatStream(req, chunk -> {
            count.incrementAndGet();
            if (chunk.getDelta() != null) collected.append(chunk.getDelta());
        });
        assertEquals(2, count.get());
        assertEquals("hello", collected.toString());
    }
}
