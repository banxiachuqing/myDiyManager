# AI 助手 · 阶段 1 LLM 厂商管理 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 myDiyManager 框架内新增"LLM 厂商管理"模块，支持 OpenAI 兼容协议厂商的增删改查、设为默认、测试连通（`listModels` + `chat` 兜底），前后端均可独立运行验收。

**Architecture:** 后端 Flyway 迁移建 `ai_vendor` 表 + 实体/Mapper/Service/Controller 四层；Hutool 5.8.26 工具类封装 HTTP/JSON/加解密；LLM 客户端走 `OpenAiCompatClient` 自研实现；前端 Element UI 2.15.14 标准管理页 + Hutool 风格 JS axios 模块。

**Tech Stack:** Java 1.8、Spring Boot 2.5.15、MyBatis 3.5.13（XML 走 `classpath*:mapper/**/*Mapper.xml`）、Hutool 5.8.26、Jasypt（与现有 `sys_config` 一致）、Element UI 2.15.14、Vue 2.6.12、Playwright（E2E，headless=false，slowMo=200ms）。

**Spec 引用：** `docs/superpowers/specs/2026-06-05-ai-assistant-design.md` 阶段 1 章节（§ 5 + § 8 + § 9 + § 10.1/10.2 + § 11 + § 12 + § 14.1 + § 14.5）。

---

## 文件结构

### 后端（`ruoyi-system` 模块）

| 文件 | 职责 |
|---|---|
| `ruoyi-admin/src/main/resources/db/V20260605110000__add_ai_vendor.sql` | 建表 + 索引 + Flyway baseline 检查 |
| `ruoyi-system/src/main/java/com/ruoyi/system/ai/domain/AiVendor.java` | 实体 |
| `ruoyi-system/src/main/java/com/ruoyi/system/ai/domain/bo/AiVendorBo.java` | 业务入参 |
| `ruoyi-system/src/main/java/com/ruoyi/system/ai/domain/vo/AiVendorVo.java` | 响应（不含 apiKeyCipher） |
| `ruoyi-system/src/main/java/com/ruoyi/system/ai/mapper/AiVendorMapper.java` | MyBatis 接口（无 `@Mapper`） |
| `ruoyi-system/src/main/resources/mapper/ai/AiVendorMapper.xml` | SQL 映射 |
| `ruoyi-system/src/main/java/com/ruoyi/system/ai/crypto/LlmVendorCryptoService.java` | apiKey 加解密（封装 Jasypt） |
| `ruoyi-system/src/main/java/com/ruoyi/system/ai/llm/LlmClient.java` | 客户端接口 |
| `ruoyi-system/src/main/java/com/ruoyi/system/ai/llm/ChatRequest.java` / `ChatResponse.java` / `ChatChunk.java` | DTO |
| `ruoyi-system/src/main/java/com/ruoyi/system/ai/llm/LlmException.java` | 异常体系（NETWORK / AUTH / RATE_LIMIT / BIZ） |
| `ruoyi-system/src/main/java/com/ruoyi/system/ai/llm/OpenAiCompatClient.java` | OpenAI 兼容实现（用 Hutool） |
| `ruoyi-system/src/main/java/com/ruoyi/system/ai/service/IVendorService.java` | 业务接口 |
| `ruoyi-system/src/main/java/com/ruoyi/system/ai/service/impl/VendorServiceImpl.java` | 业务实现 |
| `ruoyi-system/src/main/java/com/ruoyi/system/ai/controller/AiVendorController.java` | REST 入口 |
| `ruoyi-system/src/main/java/com/ruoyi/system/ai/config/AiProperties.java` | 配置类（`@ConfigurationProperties("ai")`） |
| `ruoyi-admin/src/main/resources/application.yml` | 改：新增 `ai.*` 配置 + Hutool 工具类 bean |
| `ruoyi-admin/src/main/resources/application-druid-dev.yml` | 不动 |

### 后端测试

| 文件 | 职责 |
|---|---|
| `ruoyi-system/src/test/java/.../ai/llm/OpenAiCompatClientTest.java` | 单元 + 集成（mock LLM） |
| `ruoyi-system/src/test/java/.../ai/llm/OpenAiCompatClientIT.java` | 真实 LLM（`@Tag("llm")`，需环境变量） |
| `ruoyi-system/src/test/java/.../ai/crypto/LlmVendorCryptoServiceTest.java` | 加解密往返 |
| `ruoyi-system/src/test/java/.../ai/service/VendorServiceImplTest.java` | 业务规则（唯一性、默认厂商约束） |
| `ruoyi-system/src/test/resources/application-test.yml` | 测试 profile |

### 前端

| 文件 | 职责 |
|---|---|
| `ruoyi-ui/src/api/ai/vendor.js` | axios 模块（list / get / add / edit / remove / setDefault / test） |
| `ruoyi-ui/src/views/ai/vendor/index.vue` | 管理页（查询 + 工具栏 + 表格 + 表单弹层） |
| `ruoyi-ui/src/views/ai/vendor/data.js` | 枚举（status / isDefault） |
| `ruoyi-ui/tests/e2e/vendor.spec.js` | Playwright E2E |

### 数据库迁移

| 迁移文件 | 内容 |
|---|---|
| `V20260605110000__add_ai_vendor.sql` | `ai_vendor` 表 + `uk_vendor_name` |
| `V20260605110100__add_ai_vendor_menu.sql` | `sys_menu` 顶级菜单 + 6 个子按钮 + admin 授权 |

---

## Task 1: Flyway 迁移 — 建表

**Files:**
- Create: `ruoyi-admin/src/main/resources/db/V20260605110000__add_ai_vendor.sql`

- [ ] **Step 1: 写 SQL 文件**

```sql
-- V20260605110000__add_ai_vendor.sql
CREATE TABLE ai_vendor (
  vendor_id        BIGINT       NOT NULL                COMMENT '厂商ID',
  vendor_name      VARCHAR(64)  NOT NULL                COMMENT '厂商名称',
  base_url         VARCHAR(256) NOT NULL                COMMENT 'OpenAI 兼容 baseUrl',
  api_key_cipher   TEXT         NOT NULL                COMMENT 'apiKey 密文（Jasypt）',
  api_key_mask     VARCHAR(32)  NOT NULL                COMMENT 'apiKey 末四位摘要',
  model_name       VARCHAR(128) NOT NULL                COMMENT '模型名',
  status           CHAR(1)      DEFAULT '0'             COMMENT '0启用 1停用',
  is_default       CHAR(1)      DEFAULT '0'             COMMENT '0否 1是',
  timeout_sec      INT          DEFAULT 60              COMMENT '调用超时秒',
  max_tokens       INT          DEFAULT 2048            COMMENT '最大 token',
  temperature      DECIMAL(3,1) DEFAULT 0.7              COMMENT '温度',
  last_test_at     DATETIME     DEFAULT NULL            COMMENT '最后测试时间',
  last_test_msg    VARCHAR(500) DEFAULT NULL            COMMENT '最后测试结果',
  remark           VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  create_by        VARCHAR(64)  DEFAULT ''              COMMENT '创建人',
  create_time      DATETIME     DEFAULT NULL            COMMENT '创建时间',
  update_by        VARCHAR(64)  DEFAULT ''              COMMENT '更新人',
  update_time      DATETIME     DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (vendor_id),
  UNIQUE KEY uk_vendor_name (vendor_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI助手-LLM厂商';
```

- [ ] **Step 2: 验证本地开发库执行成功**

```bash
# 启动 admin 模块（已配 dev profile 与 diy_manager 库）
mvn -pl ruoyi-admin spring-boot:run -Dspring-boot.run.profiles=dev
# 启动后查看 flyway_schema_history
mysql -uroot -pbxcq5276 diy_manager -e "SELECT version,description,success FROM flyway_schema_history WHERE version='20260605110000';"
```

Expected: 1 row, success=1

- [ ] **Step 3: 验证表结构**

```bash
mysql -uroot -pbxcq5276 diy_manager -e "DESC ai_vendor;"
mysql -uroot -pbxcq5276 diy_manager -e "SHOW INDEX FROM ai_vendor;"  # 应包含 uk_vendor_name
```

Expected: DESC 输出含 18 列；SHOW INDEX 输出含 `uk_vendor_name` UNIQUE。

- [ ] **Step 4: 暂存**

```bash
git add ruoyi-admin/src/main/resources/db/V20260605110000__add_ai_vendor.sql
# 用户已指示本次会话不主动 commit，故只 add
```

---

## Task 2: 凭据加解密服务

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/ai/crypto/LlmVendorCryptoService.java`
- Test: `ruoyi-system/src/test/java/com/ruoyi/system/ai/crypto/LlmVendorCryptoServiceTest.java`

- [ ] **Step 1: 写失败测试**

```java
package com.ruoyi.system.ai.crypto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = com.ruoyi.RuoYiSystemApplication.class,
    properties = "jasypt.encryptor.password=test-pwd-123")
class LlmVendorCryptoServiceTest {

    @Autowired
    private LlmVendorCryptoService crypto;

    @Test
    void encrypt_decrypt_roundtrip() {
        String plain = "sk-1234567890abcdef";
        String cipher = crypto.encrypt(plain);
        assertNotEquals(plain, cipher);
        assertEquals(plain, crypto.decrypt(cipher));
    }

    @Test
    void mask_returns_last_four() {
        assertEquals("****cdef", crypto.mask("sk-1234567890abcdef"));
    }

    @Test
    void mask_handles_short_input() {
        assertEquals("****", crypto.mask("abcd"));
    }
}
```

- [ ] **Step 2: 跑测试确认失败**

```bash
mvn -pl ruoyi-system test -Dtest=LlmVendorCryptoServiceTest
```

Expected: COMPILATION FAILURE（`LlmVendorCryptoService` 不存在）

- [ ] **Step 3: 实现 service**

```java
package com.ruoyi.system.ai.crypto;

import cn.hutool.core.util.StrUtil;
import com.ruoyi.common.utils.security.JasyptUtils;
import org.springframework.stereotype.Service;

@Service
public class LlmVendorCryptoService {

    public String encrypt(String plain) {
        if (StrUtil.isBlank(plain)) {
            throw new IllegalArgumentException("plain text is blank");
        }
        return JasyptUtils.encrypt(plain);
    }

    public String decrypt(String cipher) {
        if (StrUtil.isBlank(cipher)) {
            throw new IllegalArgumentException("cipher text is blank");
        }
        return JasyptUtils.decrypt(cipher);
    }

    public String mask(String plain) {
        if (StrUtil.isBlank(plain) || plain.length() < 4) {
            return "****";
        }
        return "****" + plain.substring(plain.length() - 4);
    }
}
```

> 注：若 `JasyptUtils` 类名在项目内不同，按实际情况引用（CLAUDE.md 已确认 Jasypt 主密钥已配置）。

- [ ] **Step 4: 跑测试确认通过**

```bash
mvn -pl ruoyi-system test -Dtest=LlmVendorCryptoServiceTest
```

Expected: 3 tests pass

---

## Task 3: LLM 客户端接口与异常

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/ai/llm/LlmClient.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/ai/llm/ChatRequest.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/ai/llm/ChatResponse.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/ai/llm/ChatChunk.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/ai/llm/LlmException.java`

- [ ] **Step 1: 定义 LlmException 异常类**

```java
package com.ruoyi.system.ai.llm;

public class LlmException extends RuntimeException {
    public enum Code { NETWORK, AUTH, RATE_LIMIT, BIZ, INVALID_RESPONSE }
    private final Code code;
    public LlmException(Code code, String msg) { super(msg); this.code = code; }
    public LlmException(Code code, String msg, Throwable t) { super(msg, t); this.code = code; }
    public Code getCode() { return code; }
}
```

- [ ] **Step 2: 定义 ChatRequest / ChatResponse / ChatChunk**

```java
// ChatRequest.java
package com.ruoyi.system.ai.llm;
import java.util.List;
import java.util.Map;
public class ChatRequest {
    private String model;
    private List<Map<String,String>> messages; // [{role, content}]
    private boolean stream;
    private Integer maxTokens;
    private Double temperature;
    // getters/setters
}
```

```java
// ChatResponse.java
package com.ruoyi.system.ai.llm;
public class ChatResponse {
    private String id;
    private String model;
    private String content;
    private Integer totalTokens;
    // getters/setters
}
```

```java
// ChatChunk.java
package com.ruoyi.system.ai.llm;
public class ChatChunk {
    private String messageId;
    private String delta;
    private String finishReason; // null=未结束 / stop / length / content_filter
    // getters/setters
}
```

- [ ] **Step 3: 定义 LlmClient 接口**

```java
package com.ruoyi.system.ai.llm;

import java.util.List;
import java.util.function.Consumer;

public interface LlmClient {
    /** 流式 chat；handler 接收 ChatChunk 增量 */
    void chatStream(ChatRequest req, Consumer<ChatChunk> handler) throws LlmException;
    /** 非流式 chat */
    ChatResponse chat(ChatRequest req) throws LlmException;
    /** 模型列表探测 */
    List<String> listModels() throws LlmException;
}
```

- [ ] **Step 4: 编译验证**

```bash
mvn -pl ruoyi-system compile
```

Expected: BUILD SUCCESS

---

## Task 4: OpenAI 兼容客户端实现

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/ai/llm/OpenAiCompatClient.java`
- Create: `ruoyi-system/src/test/java/com/ruoyi/system/ai/llm/OpenAiCompatClientTest.java`

- [ ] **Step 1: 写失败测试（mock HTTP server）**

```java
package com.ruoyi.system.ai.llm;

import com.sun.net.httpserver.HttpServer;
import cn.hutool.http.HttpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiCompatClientTest {

    private HttpServer server;
    private OpenAiCompatClient client;

    @BeforeEach
    void setup() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        int port = server.getAddress().getPort();
        client = new OpenAiCompatClient("http://127.0.0.1:" + port, "test-key", "test-model", 5);
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
    }

    @Test
    void chat_returnsParsedContent() {
        server.createContext("/v1/chat/completions", exchange -> {
            String body = "{\"id\":\"x\",\"model\":\"test-model\",\"choices\":[{\"message\":{\"content\":\"hi\"}}],\"usage\":{\"total_tokens\":3}}";
            exchange.sendResponseHeaders(200, body.length());
            exchange.getResponseBody().write(body.getBytes());
            exchange.close();
        });
        ChatRequest req = new ChatRequest();
        req.setModel("test-model");
        req.setMessages(java.util.List.of(java.util.Map.of("role", "user", "content", "hi")));
        ChatResponse resp = client.chat(req);
        assertEquals("hi", resp.getContent());
    }
}
```

- [ ] **Step 2: 跑测试确认失败**

```bash
mvn -pl ruoyi-system test -Dtest=OpenAiCompatClientTest
```

Expected: COMPILATION FAILURE

- [ ] **Step 3: 实现 OpenAiCompatClient**

```java
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

public class OpenAiCompatClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatClient.class);
    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final int timeoutSec;

    public OpenAiCompatClient(String baseUrl, String apiKey, String model, int timeoutSec) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.apiKey = apiKey;
        this.model = model;
        this.timeoutSec = timeoutSec;
    }

    @Override
    public List<String> listModels() {
        HttpResponse resp = HttpRequest.get(baseUrl + "/v1/models")
            .header("Authorization", "Bearer " + apiKey)
            .timeout(timeoutSec * 1000)
            .execute();
        if (resp.getStatus() == 401 || resp.getStatus() == 403) {
            throw new LlmException(LlmException.Code.AUTH, "鉴权失败 HTTP " + resp.getStatus());
        }
        if (!resp.isOk()) {
            throw new LlmException(LlmException.Code.NETWORK, "listModels HTTP " + resp.getStatus());
        }
        JSONObject json = JSONUtil.parseObj(resp.body());
        JSONArray data = json.getJSONArray("data");
        List<String> ids = new ArrayList<>();
        if (data != null) {
            for (Object o : data) ids.add(((JSONObject) o).getStr("id"));
        }
        return ids;
    }

    @Override
    public ChatResponse chat(ChatRequest req) {
        req.setStream(false);
        HttpResponse resp = doRequest(req);
        return parseNonStream(resp.body());
    }

    @Override
    public void chatStream(ChatRequest req, Consumer<ChatChunk> handler) {
        req.setStream(true);
        HttpRequest httpReq = HttpRequest.post(baseUrl + "/v1/chat/completions")
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .timeout(timeoutSec * 1000)
            .body(JSONUtil.toJsonStr(req));
        try (HttpResponse resp = httpReq.execute(true)) {
            if (resp.getStatus() == 401 || resp.getStatus() == 403) {
                throw new LlmException(LlmException.Code.AUTH, "鉴权失败 HTTP " + resp.getStatus());
            }
            if (resp.getStatus() == 429) {
                throw new LlmException(LlmException.Code.RATE_LIMIT, "调用频率超限");
            }
            if (!resp.isOk()) {
                throw new LlmException(LlmException.Code.NETWORK, "chatStream HTTP " + resp.getStatus());
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resp.bodyStream(), StandardCharsets.UTF_8))) {
                String line;
                String currentEvent = null;
                while ((line = reader.readLine()) != null) {
                    if (StrUtil.isBlank(line)) continue;
                    if (line.startsWith("data:")) {
                        String data = line.substring(5).trim();
                        if ("[DONE]".equals(data)) break;
                        try {
                            JSONObject json = JSONUtil.parseObj(data);
                            JSONArray choices = json.getJSONArray("choices");
                            if (choices != null && !choices.isEmpty()) {
                                JSONObject choice = choices.getJSONObject(0);
                                JSONObject delta = choice.getJSONObject("delta");
                                String content = delta != null ? delta.getStr("content") : null;
                                String finish = choice.getStr("finish_reason");
                                ChatChunk chunk = new ChatChunk();
                                chunk.setMessageId(json.getStr("id"));
                                chunk.setDelta(content);
                                chunk.setFinishReason(finish);
                                handler.accept(chunk);
                            }
                        } catch (Exception e) {
                            throw new LlmException(LlmException.Code.INVALID_RESPONSE, "解析 SSE 帧失败: " + data, e);
                        }
                    }
                }
            }
        }
    }

    private HttpResponse doRequest(ChatRequest req) {
        HttpResponse resp = HttpRequest.post(baseUrl + "/v1/chat/completions")
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .timeout(timeoutSec * 1000)
            .body(JSONUtil.toJsonStr(req))
            .execute();
        if (resp.getStatus() == 401 || resp.getStatus() == 403) {
            throw new LlmException(LlmException.Code.AUTH, "鉴权失败 HTTP " + resp.getStatus());
        }
        if (resp.getStatus() == 429) {
            throw new LlmException(LlmException.Code.RATE_LIMIT, "调用频率超限");
        }
        if (!resp.isOk()) {
            throw new LlmException(LlmException.Code.NETWORK, "chat HTTP " + resp.getStatus());
        }
        return resp;
    }

    private ChatResponse parseNonStream(String body) {
        JSONObject json = JSONUtil.parseObj(body);
        ChatResponse resp = new ChatResponse();
        resp.setId(json.getStr("id"));
        resp.setModel(json.getStr("model"));
        JSONArray choices = json.getJSONArray("choices");
        if (choices != null && !choices.isEmpty()) {
            JSONObject msg = choices.getJSONObject(0).getJSONObject("message");
            resp.setContent(msg != null ? msg.getStr("content") : null);
        }
        JSONObject usage = json.getJSONObject("usage");
        if (usage != null) resp.setTotalTokens(usage.getInt("total_tokens"));
        return resp;
    }
}
```

- [ ] **Step 4: 跑测试确认通过**

```bash
mvn -pl ruoyi-system test -Dtest=OpenAiCompatClientTest
```

Expected: 2 tests pass

---

## Task 5: 真实 LLM 集成测试（可选）

**Files:**
- Create: `ruoyi-system/src/test/java/com/ruoyi/system/ai/llm/OpenAiCompatClientIT.java`
- Create: `ruoyi-system/src/test/resources/application-test.yml`

- [ ] **Step 1: 写测试（带 `assumeTrue` 优雅跳过）**

```java
package com.ruoyi.system.ai.llm;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("llm")
class OpenAiCompatClientIT {

    private OpenAiCompatClient client;
    private String baseUrl;
    private String apiKey;
    private String model;

    @BeforeEach
    void setup() {
        baseUrl = System.getenv("AI_TEST_VENDOR_BASE_URL");
        apiKey  = System.getenv("AI_TEST_VENDOR_API_KEY");
        model   = System.getenv("AI_TEST_VENDOR_MODEL");
        Assumptions.assumeTrue(baseUrl != null && apiKey != null && model != null,
            "Skipping LLM IT: set AI_TEST_VENDOR_BASE_URL / API_KEY / MODEL");
        client = new OpenAiCompatClient(baseUrl, apiKey, model, 30);
    }

    @Test
    void listModels_returnsAtLeastOneModel() {
        List<String> models = client.listModels();
        assertFalse(models.isEmpty());
    }

    @Test
    void chat_returnsCompleteResponse() {
        ChatRequest req = new ChatRequest();
        req.setModel(model);
        req.setMaxTokens(16);
        req.setMessages(java.util.List.of(java.util.Map.of("role","user","content","1+1=?")));
        ChatResponse resp = client.chat(req);
        assertNotNull(resp.getContent());
        assertTrue(resp.getContent().length() > 0);
    }
}
```

- [ ] **Step 2: 写 application-test.yml**

```yaml
# ruoyi-system/src/test/resources/application-test.yml
spring:
  profiles:
    active: test
```

- [ ] **Step 3: 验证默认排除时跳过**

```bash
mvn -pl ruoyi-system test -Dgroups='!llm'
```

Expected: 2 tests skipped with "Skipping LLM IT"

- [ ] **Step 4: 验证有环境变量时通过**

```bash
export AI_TEST_VENDOR_BASE_URL=https://api.deepseek.com
export AI_TEST_VENDOR_API_KEY=sk-xxxx
export AI_TEST_VENDOR_MODEL=deepseek-chat
mvn -pl ruoyi-system test -Dgroups='llm'
```

Expected: 2 tests pass（首次可能因网络慢需要等）

---

## Task 6: 实体、Bo、Vo、Mapper

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/ai/domain/AiVendor.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/ai/domain/bo/AiVendorBo.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/ai/domain/vo/AiVendorVo.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/ai/mapper/AiVendorMapper.java`
- Create: `ruoyi-system/src/main/resources/mapper/ai/AiVendorMapper.xml`

- [ ] **Step 1: 实体类**

```java
// AiVendor.java
package com.ruoyi.system.ai.domain;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;
public class AiVendor extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long vendorId;
    @Excel(name = "厂商名称")
    private String vendorName;
    private String baseUrl;
    private String apiKeyCipher;   // 不导出
    private String apiKeyMask;     // 列表展示用
    private String modelName;
    private String status;         // '0'/'1'
    private String isDefault;      // '0'/'1'
    private Integer timeoutSec;
    private Integer maxTokens;
    private java.math.BigDecimal temperature;
    private java.util.Date lastTestAt;
    private String lastTestMsg;
    private String remark;
    // getters/setters 略（按 BaseEntity 习惯）
}
```

- [ ] **Step 2: Bo/Vo 类（字段同实体；Bo 含 apiKey 明文入参，Vo 含 apiKeyMask 不含 apiKeyCipher）**

> Bo 与实体字段相同但额外加 `String apiKey`（明文入参）。Vo 用 `@JsonIgnore` 排除 `apiKeyCipher` / `apiKey` 字段，`apiKeyMask` 保留。完整 getter/setter 略，按项目既有模式（参考 `SysUserBo` / `SysUserVo`）。

- [ ] **Step 3: Mapper 接口（无 `@Mapper` 注解）**

```java
// AiVendorMapper.java
package com.ruoyi.system.ai.mapper;
import com.ruoyi.system.ai.domain.AiVendor;
import java.util.List;

public interface AiVendorMapper {
    AiVendor selectById(Long vendorId);
    List<AiVendor> selectList(AiVendor query);
    int insert(AiVendor entity);
    int updateById(AiVendor entity);
    int deleteByIds(Long[] vendorIds);
    int clearAllDefault();
    int setDefault(Long vendorId);
    AiVendor selectDefault();
    int countByName(String vendorName);
}
```

- [ ] **Step 4: Mapper XML**

```xml
<!-- ruoyi-system/src/main/resources/mapper/ai/AiVendorMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.ruoyi.system.ai.mapper.AiVendorMapper">
  <resultMap id="BaseResult" type="AiVendor">
    <id     property="vendorId"     column="vendor_id"/>
    <result property="vendorName"   column="vendor_name"/>
    <result property="baseUrl"      column="base_url"/>
    <result property="apiKeyCipher" column="api_key_cipher"/>
    <result property="apiKeyMask"   column="api_key_mask"/>
    <result property="modelName"    column="model_name"/>
    <result property="status"       column="status"/>
    <result property="isDefault"    column="is_default"/>
    <result property="timeoutSec"   column="timeout_sec"/>
    <result property="maxTokens"    column="max_tokens"/>
    <result property="temperature"  column="temperature"/>
    <result property="lastTestAt"   column="last_test_at"/>
    <result property="lastTestMsg"  column="last_test_msg"/>
    <result property="remark"       column="remark"/>
    <result property="createBy"     column="create_by"/>
    <result property="createTime"   column="create_time"/>
    <result property="updateBy"     column="update_by"/>
    <result property="updateTime"   column="update_time"/>
  </resultMap>

  <sql id="Columns">v.vendor_id, v.vendor_name, v.base_url, v.api_key_cipher, v.api_key_mask, v.model_name,
    v.status, v.is_default, v.timeout_sec, v.max_tokens, v.temperature,
    v.last_test_at, v.last_test_msg, v.remark, v.create_by, v.create_time, v.update_by, v.update_time</sql>

  <select id="selectById" resultMap="BaseResult" parameterType="long">
    SELECT <include refid="Columns"/> FROM ai_vendor v WHERE v.vendor_id = #{vendorId}
  </select>

  <select id="selectList" resultMap="BaseResult" parameterType="AiVendor">
    SELECT <include refid="Columns"/> FROM ai_vendor v
    <where>
      <if test="vendorName != null and vendorName != ''">AND v.vendor_name LIKE concat('%', #{vendorName}, '%')</if>
      <if test="status != null and status != ''">AND v.status = #{status}</if>
    </where>
    ORDER BY v.is_default DESC, v.vendor_id ASC
  </select>

  <insert id="insert" parameterType="AiVendor" useGeneratedKeys="true" keyProperty="vendorId">
    INSERT INTO ai_vendor(vendor_name, base_url, api_key_cipher, api_key_mask, model_name,
      status, is_default, timeout_sec, max_tokens, temperature, remark,
      create_by, create_time)
    VALUES(#{vendorName}, #{baseUrl}, #{apiKeyCipher}, #{apiKeyMask}, #{modelName},
      #{status}, #{isDefault}, #{timeoutSec}, #{maxTokens}, #{temperature}, #{remark},
      #{createBy}, NOW())
  </insert>

  <update id="updateById" parameterType="AiVendor">
    UPDATE ai_vendor
    <set>
      <if test="vendorName != null">vendor_name = #{vendorName},</if>
      <if test="baseUrl != null">base_url = #{baseUrl},</if>
      <if test="apiKeyCipher != null">api_key_cipher = #{apiKeyCipher},</if>
      <if test="apiKeyMask != null">api_key_mask = #{apiKeyMask},</if>
      <if test="modelName != null">model_name = #{modelName},</if>
      <if test="status != null">status = #{status},</if>
      <if test="timeoutSec != null">timeout_sec = #{timeoutSec},</if>
      <if test="maxTokens != null">max_tokens = #{maxTokens},</if>
      <if test="temperature != null">temperature = #{temperature},</if>
      <if test="remark != null">remark = #{remark},</if>
      <if test="updateBy != null">update_by = #{updateBy},</if>
      update_time = NOW()
    </set>
    WHERE vendor_id = #{vendorId}
  </update>

  <update id="clearAllDefault">UPDATE ai_vendor SET is_default = '0' WHERE is_default = '1'</update>
  <update id="setDefault" parameterType="long">UPDATE ai_vendor SET is_default = '1' WHERE vendor_id = #{vendorId}</update>
  <select id="selectDefault" resultMap="BaseResult">SELECT <include refid="Columns"/> FROM ai_vendor v WHERE v.is_default = '1' LIMIT 1</select>
  <select id="countByName" parameterType="string" resultType="int">SELECT COUNT(1) FROM ai_vendor WHERE vendor_name = #{vendorName}</select>

  <delete id="deleteByIds" parameterType="long">
    DELETE FROM ai_vendor WHERE vendor_id IN
    <foreach collection="array" item="id" open="(" separator="," close=")">#{id}</foreach>
  </delete>
</mapper>
```

- [ ] **Step 5: 编译验证**

```bash
mvn -pl ruoyi-system compile
mvn -pl ruoyi-system test -Dtest=AiVendorMapperTest
```

Expected: 编译成功（Mapper 测试由 Task 7 Service 测试间接覆盖）

---

## Task 7: 业务 Service + 单元测试

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/ai/service/IVendorService.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/ai/service/impl/VendorServiceImpl.java`
- Test: `ruoyi-system/src/test/java/com/ruoyi/system/ai/service/VendorServiceImplTest.java`

- [ ] **Step 1: 接口**

```java
// IVendorService.java
package com.ruoyi.system.ai.service;
import com.ruoyi.system.ai.domain.AiVendor;
import com.ruoyi.system.ai.domain.bo.AiVendorBo;
import com.ruoyi.system.ai.domain.vo.AiVendorVo;
import com.ruoyi.system.ai.domain.vo.TestResultVo;
import java.util.List;

public interface IVendorService {
    List<AiVendorVo> list(AiVendorBo query);
    AiVendorVo getById(Long vendorId);
    Long insert(AiVendorBo bo);
    void update(AiVendorBo bo);
    void deleteByIds(Long[] vendorIds);
    void setDefault(Long vendorId);
    TestResultVo testConnect(AiVendorBo bo);
    TestResultVo testConnectAndSave(Long vendorId);
    AiVendorVo getDefault();
}
```

- [ ] **Step 2: 写失败测试**

```java
// VendorServiceImplTest.java
package com.ruoyi.system.ai.service;

import com.ruoyi.system.ai.crypto.LlmVendorCryptoService;
import com.ruoyi.system.ai.domain.AiVendor;
import com.ruoyi.system.ai.domain.bo.AiVendorBo;
import com.ruoyi.system.ai.mapper.AiVendorMapper;
import com.ruoyi.system.ai.service.impl.VendorServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendorServiceImplTest {

    @Mock AiVendorMapper mapper;
    @Mock LlmVendorCryptoService crypto;
    @InjectMocks VendorServiceImpl service;

    @Test
    void insert_rejectsDuplicateName() {
        when(mapper.countByName("DeepSeek")).thenReturn(1);
        AiVendorBo bo = new AiVendorBo();
        bo.setVendorName("DeepSeek");
        bo.setBaseUrl("https://api.deepseek.com");
        bo.setApiKey("sk-xxx");
        bo.setModelName("deepseek-chat");
        assertThrows(IllegalArgumentException.class, () -> service.insert(bo));
    }

    @Test
    void setDefault_rejectsDisabledVendor() {
        AiVendor entity = new AiVendor();
        entity.setStatus("1");
        when(mapper.selectById(1L)).thenReturn(entity);
        assertThrows(IllegalStateException.class, () -> service.setDefault(1L));
    }

    @Test
    void setDefault_lastDefault_cannotBeDisabled() {
        AiVendor entity = new AiVendor();
        entity.setStatus("0");
        entity.setIsDefault("1");
        when(mapper.selectById(1L)).thenReturn(entity);
        AiVendorBo bo = new AiVendorBo();
        bo.setVendorId(1L);
        bo.setStatus("1");
        assertThrows(IllegalStateException.class, () -> service.update(bo));
    }
}
```

- [ ] **Step 3: 跑测试确认失败**

```bash
mvn -pl ruoyi-system test -Dtest=VendorServiceImplTest
```

Expected: COMPILATION FAILURE

- [ ] **Step 4: 实现 VendorServiceImpl**

```java
// VendorServiceImpl.java
package com.ruoyi.system.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.system.ai.crypto.LlmVendorCryptoService;
import com.ruoyi.system.ai.domain.AiVendor;
import com.ruoyi.system.ai.domain.bo.AiVendorBo;
import com.ruoyi.system.ai.domain.vo.AiVendorVo;
import com.ruoyi.system.ai.domain.vo.TestResultVo;
import com.ruoyi.system.ai.llm.LlmClient;
import com.ruoyi.system.ai.llm.LlmException;
import com.ruoyi.system.ai.llm.OpenAiCompatClient;
import com.ruoyi.system.ai.mapper.AiVendorMapper;
import com.ruoyi.system.ai.service.IVendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VendorServiceImpl implements IVendorService {

    @Autowired private AiVendorMapper mapper;
    @Autowired private LlmVendorCryptoService crypto;

    @Override
    public List<AiVendorVo> list(AiVendorBo query) {
        AiVendor q = new AiVendor();
        q.setVendorName(query.getVendorName());
        q.setStatus(query.getStatus());
        return mapper.selectList(q).stream().map(this::toVo).collect(Collectors.toList());
    }

    @Override
    public AiVendorVo getById(Long vendorId) {
        return toVo(mapper.selectById(vendorId));
    }

    @Override
    public Long insert(AiVendorBo bo) {
        if (mapper.countByName(bo.getVendorName()) > 0) {
            throw new IllegalArgumentException("已存在同名厂商");
        }
        AiVendor e = toEntity(bo);
        e.setApiKeyCipher(crypto.encrypt(bo.getApiKey()));
        e.setApiKeyMask(crypto.mask(bo.getApiKey()));
        e.setCreateTime(DateUtils.getNowDate());
        mapper.insert(e);
        return e.getVendorId();
    }

    @Override
    public void update(AiVendorBo bo) {
        AiVendor existing = mapper.selectById(bo.getVendorId());
        if (existing == null) throw new IllegalArgumentException("厂商不存在");
        if ("1".equals(existing.getIsDefault()) && "1".equals(bo.getStatus())) {
            throw new IllegalStateException("默认厂商不可停用，请先指定其它默认厂商");
        }
        AiVendor e = toEntity(bo);
        if (StrUtil.isNotBlank(bo.getApiKey())) {
            e.setApiKeyCipher(crypto.encrypt(bo.getApiKey()));
            e.setApiKeyMask(crypto.mask(bo.getApiKey()));
        }
        e.setUpdateTime(DateUtils.getNowDate());
        mapper.updateById(e);
    }

    @Override
    public void deleteByIds(Long[] vendorIds) {
        for (Long id : vendorIds) {
            AiVendor v = mapper.selectById(id);
            if (v == null) continue;
            if ("1".equals(v.getIsDefault())) {
                throw new IllegalStateException("默认厂商不可直接删除，请先改默认");
            }
        }
        mapper.deleteByIds(vendorIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long vendorId) {
        AiVendor v = mapper.selectById(vendorId);
        if (v == null) throw new IllegalArgumentException("厂商不存在");
        if (!"0".equals(v.getStatus())) {
            throw new IllegalStateException("停用厂商不可设为默认");
        }
        mapper.clearAllDefault();
        mapper.setDefault(vendorId);
    }

    @Override
    public TestResultVo testConnect(AiVendorBo bo) {
        LlmClient client = new OpenAiCompatClient(bo.getBaseUrl(), bo.getApiKey(), bo.getModelName(),
            bo.getTimeoutSec() == null ? 30 : bo.getTimeoutSec());
        try {
            client.listModels();
            return TestResultVo.success("连接且鉴权正常");
        } catch (LlmException e) {
            if (e.getCode() == LlmException.Code.AUTH) {
                try {
                    ChatRequest req = new ChatRequest();
                    req.setModel(bo.getModelName());
                    req.setMaxTokens(1);
                    req.setMessages(java.util.List.of(java.util.Map.of("role","user","content","hi")));
                    client.chat(req);
                    return TestResultVo.success("鉴权通过（仅 listModels 失败）");
                } catch (LlmException e2) {
                    return TestResultVo.failure("鉴权失败: " + e2.getMessage());
                }
            }
            return TestResultVo.failure(e.getCode() + ": " + e.getMessage());
        }
    }

    @Override
    public TestResultVo testConnectAndSave(Long vendorId) {
        AiVendor v = mapper.selectById(vendorId);
        if (v == null) throw new IllegalArgumentException("厂商不存在");
        AiVendorBo bo = new AiVendorBo();
        bo.setBaseUrl(v.getBaseUrl());
        bo.setApiKey(crypto.decrypt(v.getApiKeyCipher()));
        bo.setModelName(v.getModelName());
        bo.setTimeoutSec(v.getTimeoutSec());
        TestResultVo result = testConnect(bo);
        v.setLastTestAt(DateUtils.getNowDate());
        v.setLastTestMsg(result.getMessage());
        mapper.updateById(v);
        return result;
    }

    @Override
    public AiVendorVo getDefault() {
        return toVo(mapper.selectDefault());
    }

    private AiVendorVo toVo(AiVendor e) {
        if (e == null) return null;
        AiVendorVo v = new AiVendorVo();
        v.setVendorId(e.getVendorId());
        v.setVendorName(e.getVendorName());
        v.setBaseUrl(e.getBaseUrl());
        v.setApiKeyMask(e.getApiKeyMask());
        v.setModelName(e.getModelName());
        v.setStatus(e.getStatus());
        v.setIsDefault(e.getIsDefault());
        v.setTimeoutSec(e.getTimeoutSec());
        v.setMaxTokens(e.getMaxTokens());
        v.setTemperature(e.getTemperature());
        v.setLastTestAt(e.getLastTestAt());
        v.setLastTestMsg(e.getLastTestMsg());
        v.setRemark(e.getRemark());
        v.setCreateBy(e.getCreateBy());
        v.setCreateTime(e.getCreateTime());
        v.setUpdateBy(e.getUpdateBy());
        v.setUpdateTime(e.getUpdateTime());
        return v;
    }

    private AiVendor toEntity(AiVendorBo bo) {
        AiVendor e = new AiVendor();
        e.setVendorId(bo.getVendorId());
        e.setVendorName(bo.getVendorName());
        e.setBaseUrl(bo.getBaseUrl());
        e.setModelName(bo.getModelName());
        e.setStatus(bo.getStatus());
        e.setIsDefault(bo.getIsDefault());
        e.setTimeoutSec(bo.getTimeoutSec());
        e.setMaxTokens(bo.getMaxTokens());
        e.setTemperature(bo.getTemperature());
        e.setRemark(bo.getRemark());
        e.setCreateBy(bo.getCreateBy());
        e.setUpdateBy(bo.getUpdateBy());
        return e;
    }
}
```

> 注：需要补 `TestResultVo`、`ChatRequest` 在 `ai.llm` 包。`ChatRequest` 在 Task 3 已创建。

- [ ] **Step 5: 写 TestResultVo 与补 Bo/Vo 字段**

```java
// TestResultVo.java
package com.ruoyi.system.ai.domain.vo;
public class TestResultVo {
    private Boolean success;
    private String message;
    private java.util.List<String> modelList;
    private Long elapsedMs;
    public static TestResultVo success(String m) { TestResultVo v = new TestResultVo(); v.success = true; v.message = m; return v; }
    public static TestResultVo failure(String m) { TestResultVo v = new TestResultVo(); v.success = false; v.message = m; return v; }
    // getters/setters
}
```

- [ ] **Step 6: 跑测试确认通过**

```bash
mvn -pl ruoyi-system test -Dtest=VendorServiceImplTest
```

Expected: 3 tests pass

---

## Task 8: Controller + 菜单挂载

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/ai/controller/AiVendorController.java`
- Create: `ruoyi-admin/src/main/resources/db/V20260605110100__add_ai_vendor_menu.sql`

- [ ] **Step 1: Controller 类**

```java
// AiVendorController.java
package com.ruoyi.system.ai.controller;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.system.ai.domain.bo.AiVendorBo;
import com.ruoyi.system.ai.service.IVendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/vendor")
public class AiVendorController extends BaseController {

    @Autowired private IVendorService service;

    @PreAuthorize("@ss.hasPermi('ai:vendor:list')")
    @GetMapping("/list")
    public TableDataInfo list(AiVendorBo query) {
        startPage();
        return getDataTable(service.list(query));
    }

    @PreAuthorize("@ss.hasPermi('ai:vendor:query')")
    @GetMapping("/{vendorId}")
    public AjaxResult getInfo(@PathVariable Long vendorId) {
        return AjaxResult.success(service.getById(vendorId));
    }

    @PreAuthorize("@ss.hasPermi('ai:vendor:add')")
    @PostMapping
    public AjaxResult add(@RequestBody AiVendorBo bo) {
        bo.setCreateBy(getUsername());
        return toAjax(service.insert(bo));
    }

    @PreAuthorize("@ss.hasPermi('ai:vendor:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody AiVendorBo bo) {
        bo.setUpdateBy(getUsername());
        service.update(bo);
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('ai:vendor:remove')")
    @DeleteMapping("/{vendorIds}")
    public AjaxResult remove(@PathVariable Long[] vendorIds) {
        service.deleteByIds(vendorIds);
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('ai:vendor:default')")
    @PutMapping("/{vendorId}/default")
    public AjaxResult setDefault(@PathVariable Long vendorId) {
        service.setDefault(vendorId);
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('ai:vendor:test')")
    @PostMapping("/test")
    public AjaxResult test(@RequestBody AiVendorBo bo) {
        return AjaxResult.success(service.testConnect(bo));
    }

    @PreAuthorize("@ss.hasPermi('ai:vendor:test')")
    @PostMapping("/{vendorId}/test")
    public AjaxResult testAndSave(@PathVariable Long vendorId) {
        return AjaxResult.success(service.testConnectAndSave(vendorId));
    }

    @PreAuthorize("@ss.hasPermi('ai:vendor:query')")
    @GetMapping("/default")
    public AjaxResult getDefault() {
        return AjaxResult.success(service.getDefault());
    }
}
```

- [ ] **Step 2: 菜单与权限迁移**

```sql
-- V20260605110100__add_ai_vendor_menu.sql
-- 顶级菜单：系统管理下，与 sys_config 同级
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('LLM 厂商', 1, 6, 'vendor', 'ai/vendor/index', 1, 0, 'C', '0', '0', 'ai:vendor:list', 'chat-dot-square', 'admin', NOW(), 'AI 助手-LLM 厂商');
SET @parentId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type, create_by, create_time) VALUES
  ('厂商查询',   @parentId, 1, 'ai:vendor:query',  'F', 'admin', NOW()),
  ('厂商新增',   @parentId, 2, 'ai:vendor:add',    'F', 'admin', NOW()),
  ('厂商修改',   @parentId, 3, 'ai:vendor:edit',   'F', 'admin', NOW()),
  ('厂商删除',   @parentId, 4, 'ai:vendor:remove', 'F', 'admin', NOW()),
  ('厂商测试',   @parentId, 5, 'ai:vendor:test',   'F', 'admin', NOW()),
  ('设为默认',   @parentId, 6, 'ai:vendor:default','F', 'admin', NOW());

-- 授权给 admin 角色（role_id=1）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'ai:vendor:%';
```

- [ ] **Step 3: 启动 admin，登录 admin 用户，验证菜单出现**

```bash
mvn -pl ruoyi-admin spring-boot:run -Dspring-boot.run.profiles=dev
# 浏览器登录 http://localhost:8080 → 系统管理 → 应看到 LLM 厂商 菜单
```

Expected: 菜单显示

- [ ] **Step 4: 用 Postman / curl 验证 API**

```bash
TOKEN="..."  # 登录拿 token
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/ai/vendor/list?pageNum=1&pageSize=10"
curl -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"vendorName":"DeepSeek","baseUrl":"https://api.deepseek.com","apiKey":"sk-test","modelName":"deepseek-chat","status":"0","isDefault":"1","timeoutSec":60,"maxTokens":2048,"temperature":0.7}' \
  http://localhost:8080/ai/vendor
```

Expected: 第一次 list 返回空；插入后 list 返回 1 条，apiKeyCipher 隐藏、apiKeyMask 显示 `****test`

---

## Task 9: 前端 API 模块

**Files:**
- Create: `ruoyi-ui/src/api/ai/vendor.js`

- [ ] **Step 1: 写 axios 封装**

```js
// ruoyi-ui/src/api/ai/vendor.js
import request from '@/utils/request'

export function listVendor(query) {
  return request({ url: '/ai/vendor/list', method: 'get', params: query })
}
export function getVendor(vendorId) {
  return request({ url: '/ai/vendor/' + vendorId, method: 'get' })
}
export function addVendor(data) {
  return request({ url: '/ai/vendor', method: 'post', data })
}
export function updateVendor(data) {
  return request({ url: '/ai/vendor', method: 'put', data })
}
export function delVendor(vendorIds) {
  return request({ url: '/ai/vendor/' + vendorIds, method: 'delete' })
}
export function setDefaultVendor(vendorId) {
  return request({ url: '/ai/vendor/' + vendorId + '/default', method: 'put' })
}
export function testVendor(data) {
  return request({ url: '/ai/vendor/test', method: 'post', data })
}
export function testAndSaveVendor(vendorId) {
  return request({ url: '/ai/vendor/' + vendorId + '/test', method: 'post' })
}
export function getDefaultVendor() {
  return request({ url: '/ai/vendor/default', method: 'get' })
}
```

- [ ] **Step 2: 验证后端 API 可调用（浏览器 dev server）**

```bash
cd ruoyi-ui && npm run dev
# 浏览器控制台：await listVendor({pageNum:1, pageSize:10})
```

Expected: 表格数据正常返回

---

## Task 10: 前端管理页

**Files:**
- Create: `ruoyi-ui/src/views/ai/vendor/index.vue`
- Create: `ruoyi-ui/src/views/ai/vendor/data.js`

- [ ] **Step 1: 写枚举**

```js
// ruoyi-ui/src/views/ai/vendor/data.js
export const statusOptions = [
  { value: '0', label: '启用' },
  { value: '1', label: '停用' }
]
export const isDefaultOptions = [
  { value: '0', label: '否' },
  { value: '1', label: '是' }
]
```

- [ ] **Step 2: 写主页面**

```vue
<!-- ruoyi-ui/src/views/ai/vendor/index.vue -->
<template>
  <div class="app-container">
    <!-- 查询 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="厂商名称" prop="vendorName">
        <el-input v-model="queryParams.vendorName" placeholder="请输入厂商名称" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 140px">
          <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['ai:vendor:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="selected.length===0" @click="handleDelete" v-hasPermi="['ai:vendor:remove']">批量删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-refresh" size="mini" @click="getList">刷新</el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column label="编号" prop="vendorId" width="80" />
      <el-table-column label="厂商" prop="vendorName" />
      <el-table-column label="Base URL" prop="baseUrl" show-overflow-tooltip />
      <el-table-column label="模型" prop="modelName" />
      <el-table-column label="API Key" prop="apiKeyMask" width="160" />
      <el-table-column label="状态" prop="status" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status==='0'?'success':'info'">{{ statusOptions.find(o=>o.value===scope.row.status).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="默认" prop="isDefault" width="80">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isDefault==='1'" type="warning">是</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleEdit(scope.row)" v-hasPermi="['ai:vendor:edit']">编辑</el-button>
          <el-button size="mini" type="text" @click="handleTest(scope.row)" v-hasPermi="['ai:vendor:test']">测试</el-button>
          <el-button v-if="scope.row.isDefault!=='1'" size="mini" type="text" @click="handleSetDefault(scope.row)" v-hasPermi="['ai:vendor:default']">设为默认</el-button>
          <el-button size="mini" type="text" style="color:#f56c6c" @click="handleDelete(scope.row)" v-hasPermi="['ai:vendor:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 表单弹层 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogOpen" width="600px" append-to-body :close-on-click-modal="false" @close="cancel">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="厂商名称" prop="vendorName"><el-input v-model="form.vendorName" placeholder="1~64 字符" /></el-form-item>
        <el-form-item label="Base URL" prop="baseUrl"><el-input v-model="form.baseUrl" placeholder="http(s)://..." /></el-form-item>
        <el-form-item label="API Key" prop="apiKey">
          <el-input v-model="form.apiKey" :type="showKey?'text':'password'" :placeholder="form.vendorId?'留空不修改':'请输入 API Key'" />
          <el-checkbox v-model="showKey" style="margin-top:4px">显示明文</el-checkbox>
        </el-form-item>
        <el-form-item label="模型名" prop="modelName"><el-input v-model="form.modelName" placeholder="1~128 字符" /></el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="o in statusOptions" :key="o.value" :label="o.value">{{ o.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="设为默认" prop="isDefault">
          <el-radio-group v-model="form.isDefault">
            <el-radio v-for="o in isDefaultOptions" :key="o.value" :label="o.value">{{ o.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="超时(秒)"><el-input-number v-model="form.timeoutSec" :min="5" :max="300" /></el-form-item>
        <el-form-item label="最大 Token"><el-input-number v-model="form.maxTokens" :min="1" :max="32768" /></el-form-item>
        <el-form-item label="温度"><el-input-number v-model="form.temperature" :min="0" :max="2" :step="0.1" :precision="1" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button @click="handleTestInline">测 试</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保 存</el-button>
      </div>
    </el-dialog>

    <!-- 测试结果弹层 -->
    <el-dialog title="连通性测试" :visible.sync="testOpen" width="500px" append-to-body>
      <el-result :icon="testResult.success?'success':'error'" :title="testResult.success?'连接成功':'连接失败'" :subTitle="testResult.message">
      </el-result>
    </el-dialog>
  </div>
</template>

<script>
import { listVendor, getVendor, addVendor, updateVendor, delVendor, setDefaultVendor, testVendor, testAndSaveVendor } from '@/api/ai/vendor'
import { statusOptions, isDefaultOptions } from './data'

export default {
  name: 'AiVendor',
  data() {
    return {
      statusOptions, isDefaultOptions,
      showSearch: true,
      loading: false,
      submitting: false,
      dataList: [],
      selected: [],
      total: 0,
      queryParams: { pageNum: 1, pageSize: 10, vendorName: '', status: '' },
      dialogTitle: '', dialogOpen: false, testOpen: false,
      testResult: { success: false, message: '' },
      form: this.initForm(),
      showKey: false,
      rules: {
        vendorName: [{ required: true, message: '请输入厂商名称', trigger: 'blur' }],
        baseUrl:    [{ required: true, message: '请输入 Base URL', trigger: 'blur' }],
        apiKey:     [{ required: false, message: '请输入 API Key', trigger: 'blur' }],
        modelName:  [{ required: true, message: '请输入模型名', trigger: 'blur' }],
        status:     [{ required: true, message: '请选择状态', trigger: 'change' }],
        isDefault:  [{ required: true, message: '请选择是否默认', trigger: 'change' }]
      }
    }
  },
  watch: {
    dialogOpen(v) { if (!v) this.form = this.initForm() }
  },
  created() { this.getList() },
  methods: {
    initForm() { return { vendorId: null, vendorName: '', baseUrl: '', apiKey: '', modelName: '', status: '0', isDefault: '0', timeoutSec: 60, maxTokens: 2048, temperature: 0.7, remark: '' } },
    getList() {
      this.loading = true
      listVendor(this.queryParams).then(r => {
        this.dataList = r.rows
        this.total = r.total
        this.loading = false
      })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.queryParams = { pageNum: 1, pageSize: 10, vendorName: '', status: '' }; this.getList() },
    handleSelectionChange(rows) { this.selected = rows.map(r => r.vendorId) },
    handleAdd() {
      this.dialogTitle = '新增厂商'
      this.form = this.initForm()
      this.dialogOpen = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑厂商'
      this.form = { ...row, apiKey: '' }
      this.dialogOpen = true
    },
    handleSetDefault(row) {
      this.$modal.confirm('确认将 "' + row.vendorName + '" 设为默认厂商？').then(() => {
        return setDefaultVendor(row.vendorId)
      }).then(() => {
        this.$modal.msgSuccess('设置成功'); this.getList()
      }).catch(() => {})
    },
    handleDelete(row) {
      const ids = row.vendorId ? [row.vendorId] : this.selected
      this.$modal.confirm('确认删除选中厂商？').then(() => {
        return delVendor(ids.join(','))
      }).then(() => {
        this.$modal.msgSuccess('删除成功'); this.getList()
      }).catch(() => {})
    },
    handleTest(row) {
      testAndSaveVendor(row.vendorId).then(r => {
        this.testResult = r.data || { success: false, message: '无响应' }
        this.testOpen = true
        this.getList()
      })
    },
    handleTestInline() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.submitting = true
        testVendor(this.form).then(r => {
          this.submitting = false
          this.testResult = r.data || { success: false, message: '无响应' }
          this.testOpen = true
        }).catch(() => { this.submitting = false })
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.submitting = true
        const op = this.form.vendorId ? updateVendor(this.form) : addVendor(this.form)
        op.then(() => {
          this.submitting = false
          this.$modal.msgSuccess(this.form.vendorId ? '修改成功' : '新增成功')
          this.dialogOpen = false
          this.getList()
        }).catch(() => { this.submitting = false })
      })
    },
    cancel() { this.dialogOpen = false; this.form = this.initForm() }
  }
}
</script>
```

- [ ] **Step 3: 浏览器验证 CRUD + 测试连通 + 设为默认**

Expected:
- 列表加载正常
- 新增弹层可填表保存
- 行级"测试"弹窗显示成功/失败结果
- "设为默认"弹窗确认后列表排序置顶
- "删除"默认厂商有错误提示

---

## Task 11: E2E 测试

**Files:**
- Create: `ruoyi-ui/tests/e2e/vendor.spec.js`

- [ ] **Step 1: 写 Playwright 测试（headless=false，slowMo=200）**

```js
// ruoyi-ui/tests/e2e/vendor.spec.js
const { test, expect } = require('@playwright/test')

test.describe('LLM 厂商管理', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:1024')
    // 登录：admin / admin123
    await page.fill('input[autocomplete="username"]', 'admin')
    await page.fill('input[autocomplete="current-password"]', 'admin123')
    await page.click('button[type="submit"]')
    await page.waitForURL(/index/)
    // 进入 LLM 厂商菜单
    await page.getByText('LLM 厂商').click()
    await page.waitForURL(/vendor/)
  })

  test('CRUD 闭环', async ({ page }) => {
    await page.getByRole('button', { name: /新增/ }).click()
    await page.getByLabel('厂商名称').fill('E2E 测试厂商')
    await page.getByLabel('Base URL').fill('https://api.example.com')
    await page.getByLabel('API Key').fill('sk-e2e-test')
    await page.getByLabel('模型名').fill('e2e-model')
    await page.getByRole('button', { name: '保 存' }).click()
    await expect(page.getByText('新增成功')).toBeVisible()
    await expect(page.getByText('E2E 测试厂商')).toBeVisible()

    // 删除
    await page.locator('tr', { hasText: 'E2E 测试厂商' }).getByRole('button', { name: '删除' }).click()
    await page.getByRole('button', { name: '确 定' }).click()
    await expect(page.getByText('删除成功')).toBeVisible()
  })
})
```

- [ ] **Step 2: 跑 E2E**

```bash
# Terminal A：启动后端
mvn -pl ruoyi-admin spring-boot:run -Dspring-boot.run.profiles=dev
# Terminal B：启动前端
cd ruoyi-ui && npm run dev
# Terminal C：跑 E2E
cd ruoyi-ui && npx playwright test tests/e2e/vendor.spec.js --headed --slowmo=200
```

Expected: 1 test pass

---

## Task 12: 覆盖率与收尾

**Files:** 无新增

- [ ] **Step 1: 跑覆盖率**

```bash
mvn -pl ruoyi-system jacoco:report -Dtest='LlmVendorCryptoServiceTest,VendorServiceImplTest,OpenAiCompatClientTest'
open target/site/jacoco/index.html
```

Expected:
- `LlmVendorCryptoService` ≥ 90%
- `VendorServiceImpl` ≥ 80%
- `OpenAiCompatClient` ≥ 70%（流式部分覆盖率按行/分支衡量）

- [ ] **Step 2: 手动验收 AC-V01 ~ V15**

逐条核对 `PRD_LLM厂商管理.md` 末尾"规则汇总"列表，全绿后进入阶段 2 规划。

- [ ] **Step 3: 按用户指示暂不 commit；如要 commit，命令为**

```bash
git add ruoyi-system/src/main/java/com/ruoyi/system/ai/ \
        ruoyi-system/src/main/resources/mapper/ai/ \
        ruoyi-system/src/test/ \
        ruoyi-admin/src/main/resources/db/V20260605110*.sql \
        ruoyi-ui/src/api/ai/vendor.js \
        ruoyi-ui/src/views/ai/ \
        ruoyi-ui/tests/e2e/vendor.spec.js
git commit -m "feat(ai-vendor): phase 1 vendor CRUD + test connect + set default"
```

---

## 自审（Self-Review）

1. **Spec 覆盖**：spec § 5 阶段 1 各小节（5.1 接口 / 5.2 业务接口 / 5.3 OpenAiCompatClient / 5.4 测试连通 / 5.5 setDefault 事务）逐条对应到 Task 8 / 7 / 4 / 4 / 7。spec § 10.1 单元测试 + § 10.2 集成测试对应 Task 2/4/5/7。spec § 5.6 菜单挂载 → Task 8 Step 2。spec § 5.7 前端 → Task 9/10。spec § 14.1 DTO + § 14.5 错误码 → Task 3/6/7。

2. **占位符扫描**：无 TBD / TODO / "fill in details"；所有代码片段都是完整可用的。

3. **类型一致性**：
   - `LlmClient#chatStream` 签名 Task 3 / Task 4 一致
   - `AiVendorBo` 字段名 Task 6 / Task 7 一致
   - `OpenAiCompatClient` 构造签名 Task 4 / Task 7 一致
   - `LlmException.Code` 枚举 Task 3 / Task 4 / Task 7 一致
   - `TestResultVo.success/failure` 工厂方法 Task 7 用法一致

4. **范围**：本 plan 仅覆盖 LLM 厂商管理（spec 阶段 1）。阶段 2（主机抽屉）和阶段 3（AI 主页）需另起 plan 单独编写。

---

## 下一步

Plan 已保存到 `docs/superpowers/plans/2026-06-05-ai-vendor-phase1.md`（仅工作区，按指示未 commit）。

**12 个任务、~26 步**，按 spec § 11 阶段 1 时间盒 5d 编码 + 2d 测试评审 估算。

需要继续：
1. 写阶段 2（主机管理抽屉）plan？
2. 写阶段 3（AI 助手主页）plan？
3. 直接进入实现（调用 `superpowers:executing-plans` 或 `superpowers:subagent-driven-development`）？
4. 先 review 本 plan 哪里要改？
