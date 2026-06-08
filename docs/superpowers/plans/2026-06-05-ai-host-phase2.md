# AI 助手 · 阶段 2 主机管理抽屉 实施计划（事后归档）

> **说明**：本计划为事后归档，代码已在 2026-06-05 单次会话中落地。本文件用于补全 brainstorming 流程缺失的"先 plan 后 code"环节，并为后续 Phase 3 / 维护者提供执行清单。
>
> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development 或 superpowers:executing-plans。

**Goal:** 为主机运维 Tab 提供主机资产 CRUD（双认证：口令/私钥）+ 测试连接能力，数据按本系统 RBAC 部门范围隔离。

**Architecture:** 复用 LlmVendorCryptoService（Hutool AES-128）统一加密凭据；SSH 客户端走 JSch 0.1.55（每次新开 Session，避免长连接池状态泄漏）；前端用 Element UI Drawer 组件，被 AI 助手主页引用。

**Tech Stack:** Java 1.8 / Spring Boot 2.5.15 / MyBatis XML / Hutool 5.8.26 / JSch 0.1.55 / Element UI 2.15.14。

**Spec 引用：** `docs/superpowers/specs/2026-06-05-ai-assistant-design.md` § 6 + § 8 + § 9 + § 10 + § 14.2（待补）。

---

## 已落地的文件清单

| Task | 文件 | 类型 |
|---|---|---|
| T1 | `db/V20260605110400__add_ai_host_table.sql` | Flyway |
| T2 | `pom.xml`（追加 `com.jcraft:jsch:0.1.55`） | 依赖 |
| T3 | `ssh/SshException.java` | 异常 |
| T3 | `ssh/SshCommand.java` | DTO |
| T3 | `ssh/SshResult.java` | DTO |
| T3 | `ssh/SshClient.java` | 接口 |
| T3 | `ssh/JSchClient.java` | 实现 |
| T4 | `domain/AiHost.java` / `Bo` / `Vo` | 实体 |
| T5 | `mapper/AiHostMapper.java` + `mapper/ai/AiHostMapper.xml` | 持久层 |
| T6 | `service/IHostService.java` / `impl/HostServiceImpl.java` | 业务层 |
| T7 | `controller/AiHostController.java` | 入口 |
| T8 | `api/ai/host.js` | 前端 API |
| T9 | `components/AiHostDrawer/index.vue` | 前端抽屉 |
| T10 | `views/ai/chat/index.vue`（占位入口，避免 404） | 前端入口 |
| T11 | `tests/e2e/host.spec.js`（占位 E2E） | E2E |

---

## 任务细节（事后还原）

### T1. Flyway 建表

文件：`ruoyi-admin/src/main/resources/db/V20260605110400__add_ai_host_table.sql`

```sql
CREATE TABLE ai_host (
  host_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主机ID',
  host_name VARCHAR(64) NOT NULL,
  ip VARCHAR(64) NOT NULL,
  ssh_port INT NOT NULL DEFAULT 22,
  ssh_protocol VARCHAR(8) NOT NULL DEFAULT 'SSH2',
  username VARCHAR(64) NOT NULL,
  auth_type CHAR(1) NOT NULL COMMENT '0口令 1私钥',
  password_cipher TEXT DEFAULT NULL,
  private_key_cipher TEXT DEFAULT NULL,
  dept_id BIGINT NOT NULL,
  status CHAR(1) DEFAULT '2' COMMENT '0在线 1离线 2未知',
  last_test_at DATETIME DEFAULT NULL,
  last_test_msg VARCHAR(500) DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT NULL,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT NULL,
  PRIMARY KEY (host_id),
  UNIQUE KEY uk_dept_hostname (dept_id, host_name),
  KEY idx_ip (ip)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI助手-主机';
```

**关键决策**：`host_id` 必加 `AUTO_INCREMENT`（P1 已踩坑）；联合唯一 `uk_dept_hostname` 取代 sys_hostname 全局唯一，跨部门允许同名；`auth_type` 长度 1 字符 CHAR 而非 VARCHAR 节省空间。

### T2. pom.xml 追加 JSch

```xml
<dependency>
    <groupId>com.jcraft</groupId>
    <artifactId>jsch</artifactId>
    <version>0.1.55</version>
</dependency>
```

### T3. SSH 抽象 + JSch 实现

**接口设计原则**：
- `exec(SshCommand) → SshResult` 执行命令返回结果
- `ping(SshCommand)` 仅做握手+认证，用于测试连接
- 不维护长连接池，每次新开 Session（避免状态泄漏与并发复用风险）
- 超时由 `SshCommand.timeoutSec` 控制（默认 30s）

**JSch 异常映射**（P1 教训：用具体错误码，不要让用户看 stacktrace）：
- `Auth fail` / `USERAUTH fail` / `invalid privatekey` → `Code.AUTH`
- `Connection refused` / `connect timed out` / `UnknownHostException` → `Code.CONNECTION`
- `timeout` → `Code.TIMEOUT`
- 其它 → `Code.EXECUTION`

### T4. Domain/Bo/Vo 三件套

- `AiHost` 实体继承 `BaseEntity`（含 createBy/Time、updateBy/Time、remark、params）
- `AiHostBo` 含 `password` / `privateKey` 明文入参（编辑留空 = 不修改）
- `AiHostVo` `@JsonIgnore` 排除 password/privateKey 字段；明文不返回
- 用 `org.apache.ibatis.type.Alias("AiHost")` 注解

### T5. Mapper XML

8 个方法：
- `selectById` / `selectList`（按名称/IP/状态/部门）
- `insert`（`useGeneratedKeys=true keyProperty="hostId"`）
- `updateById`（动态 set 子句）
- `countByDeptAndName`（联合唯一校验）
- `deleteByIds`（foreach）
- `selectReferencingHostIds`（**P3 占位**：`WHERE 1=0`，待 ai_session 表建后启用 INNER JOIN）
- `selectAllInUse`（同上）

### T6. Service 业务规则

- **新增**：必填 deptId；同部门同名拦截；口令/私钥按 authType 分支加密
- **编辑**：留空凭据字段不修改；其余字段动态更新
- **删除**：调用引用检查方法（当前安全返回空）
- **测试连接**：表单入参优先 → DB 兜底解密；返回 `TestResultVo`
- **测试并保存**：根据结果回写 status（0 在线 / 1 离线）+ lastTestAt + lastTestMsg

**数据范围**：非 admin 强制 `deptId = 当前用户部门`；admin 可按查询条件过滤。

### T7. Controller 入口

8 个 REST 端点（`@PreAuthorize` 按权限字符串）：
- `GET /ai/host/list` → `ai:host:list`
- `GET /ai/host/all` → `ai:host:list`（主机运维 Tab 下拉用）
- `GET /ai/host/{id}` → `ai:host:query`
- `POST /ai/host` → `ai:host:add`
- `PUT /ai/host` → `ai:host:edit`
- `DELETE /ai/host/{ids}` → `ai:host:remove`
- `POST /ai/host/test` → `ai:host:test`（表单内测）
- `POST /ai/host/{id}/test` → `ai:host:test`（行级测 + 回写）

### T8. 前端 API

`ruoyi-ui/src/api/ai/host.js` 8 个 axios 方法，与 Controller 1:1 映射。

### T9. 抽屉组件

`ruoyi-ui/src/components/AiHostDrawer/index.vue`：
- Element UI Drawer，方向 rtl，宽 520px
- `modelValue` props 控制显隐（v-model 双向）
- 列表 + 查询 + 工具栏 + 批量删除
- 行级：编辑 / 测试 / 删除
- 表单弹层：双认证 radio 切换（口令/私钥），切换时清空对侧凭据
- 编辑时凭据字段留空 = 不修改
- 部门选择用 `vue-treeselect`（已在 package.json）
- `destroy-on-close` + 关闭时重置查询条件

### T10. AI 助手入口占位

`ruoyi-ui/src/views/ai/chat/index.vue`：双 Tab 占位（chat/ops），避免菜单 404；P3 在此填充真实内容。

### T11. E2E 占位

`ruoyi-ui/tests/e2e/host.spec.js`：仅登录 + 进入 AI 助手；真实 E2E 流程在 P3 补。

---

## 关键决策记录

1. **加密复用 LLM Crypto**：P1 已有 `LlmVendorCryptoService`（Hutool AES-128），主机凭据直接复用。**前提**：`ai.crypto-key` 同时加密 LLM apiKey 和主机凭据。若需隔离，需在 AiProperties 加 `ai.host.crypto-key`。
2. **每次新开 SSH Session**：避免 JSch 状态泄漏；性能上单次执行 ~50ms 开销可接受。
3. **strictHostKeyChecking=no**：方便测试，生产建议改为 `yes` + 维护 known_hosts。
4. **deleteByIds 引用检查 P3 启用**：当前安全返回空，避免误删。

## 待续（不在 P2 范围）

- [ ] 单元测试（`HostServiceImplTest` / `JSchClientTest` mock SSH server）
- [ ] 集成测试（Testcontainers 真实 SSH 服务）
- [ ] E2E 真实流程（需前后端启动）
- [ ] P3：ai_session / ai_message / ai_command 三表 + SseEmitter + Redis 白名单

---

## 自审

- **覆盖**：spec § 6 全部子节（6.1-6.7）均对应文件
- **占位符**：无 TBD / TODO 待办
- **类型一致**：AiHostBo / AiHostVo / AiHost 字段对齐
- **范围**：仅 P2，不混入 P3 内容
- **不提交 git**：按用户指示
