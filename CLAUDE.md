# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> **Before editing/creating code, query the local codegraph index first.** A live SQLite knowledge graph lives at `.codegraph/codegraph.db` (~480 files, 7.4K nodes, 15K edges, updates within ~1s of file writes). Use it for "where is X", "what calls Y", "what would break if I change Z", "show me the dependency graph" — it's much faster and more accurate than `grep` + `read` loops. MCP tools: `mcp__codegraph__codegraph_{explore,search,nodes,files,callers,callees,impact,status}`. Raw SQL fallback: `sqlite3 .codegraph/codegraph/db ".tables"`.

## 项目身份

**RuoYi-Vue 3.8.8** — 基于 SpringBoot + Vue 的后台管理系统框架（fork 自 [y_project/RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue)）。本仓库是用户在原框架基础上的**业务定制版（myDiyManager）**。

**技术栈版本**（pom 锁定）：

| 组件 | 版本 |
|---|---|
| Java | **1.8**（`<java.version>1.8</java.version>`，编译目标必须 ≤ 1.8，禁用 `List.of` / `Map.of` / `StandardCharsets.UTF_8`） |
| Spring Boot | 2.5.15 |
| Spring Security | 5.7.12 |
| MyBatis | 3.5.13（用 `org.apache.ibatis.annotations.Mapper`） |
| MySQL 驱动 | 8.0.33 |
| Druid | 1.2.23 |
| Redis | Lettuce 6.1.10 |
| 前端 | Vue 2.6.12 + Element UI 2.15.14 + Vuex 3 |

## 模块结构（Maven 多模块）

```
myDiyManager/                      ← 根 pom, 统一依赖版本
├── ruoyi-admin/                   ← 启动模块 (Spring Boot main, RuoYiApplication.java)
├── ruoyi-framework/               ← 框架核心 (Security, MyBatis, Web 配置; @ss.hasPermi() 在这里)
├── ruoyi-system/                  ← 业务模块 (sys_*, biz_*, 自定义业务代码也放这里)
├── ruoyi-quartz/                  ← 定时任务
├── ruoyi-generator/               ← 代码生成器 (Velocity 模板)
├── ruoyi-common/                  ← 公共工具类 (注解、常量、JSON、ServletUtils)
└── ruoyi-ui/                      ← Vue 前端 (Element UI, 端口 1024 dev)
```

**调用链**: Controller (`ruoyi-admin/web/controller/`) → Service (`ruoyi-system/service/`) → Mapper (`ruoyi-system/mapper/`) → MyBatis XML (`resources/mapper/**/*Mapper.xml`)

## 关键约定（不是从单文件能看出来的）

**Top-引用核心类**（codegraph 数据，**改这些前要格外小心**）：
- `com.ruoyi.common.core.domain.AjaxResult` — 全项目 179 处引用，几乎所有 Controller 返回值
- `com.ruoyi.common.core.domain.entity.{SysUser, SysMenu, SysRole, SysDept, SysConfig}` — RBAC + 业务核心实体
- `com.ruoyi.common.utils.StringUtils` — 字符串/判空/转换工具，被 74 个类引用
- Mapper **不**用 `@Mapper` 接口（codegraph 统计 0 个 `@Mapper` 节点），全部走 XML `classpath*:mapper/**/*Mapper.xml`

1. **Mapper XML 扫描路径固定**：`classpath*:mapper/**/*Mapper.xml`（application-dev.yml），与代码里 Mapper 接口在同包。`@Mapper` 注解不是必须。
2. **权限检查用自定义 SpEL**：必须写 `@PreAuthorize("@ss.hasPermi('system:user:list')"`，**不是** `hasAuthority('xxx')`——后者查 Spring Security 内置 authorities，不查 RuoYi 菜单权限表。
3. **typeAliasesPackage**: `com.ruoyi.**.domain`（用 `**` 通配所有子模块的 domain 类）。
4. **菜单权限约定**：表 `sys_menu.perms` 字段（如 `system:user:list`）→ 通过 `sys_role_menu` 关联到角色 → 登录时塞进 token.permissions。
5. **响应包装**：`com.ruoyi.common.core.domain.AjaxResult`（不是 Spring 的 `ResponseEntity`），`TableDataInfo` 用于分页。
6. **Flyway 迁移命名**：`V<YYYYMMDD><HHMMSS>__<name>.sql`，放 `ruoyi-admin/src/main/resources/db/`。
7. **`@Transactional` 默认用 `@Transactional(rollbackFor = Exception.class)`**（看现有 Service 实现）。
8. **Service 接口 `IXxxService`，实现 `XxxServiceImpl`**（注意 Impl 后缀，不是通常的 `XxxServiceImpl`）。

## 开发命令

```bash
# 后端启动 (dev profile, 需要本地 MySQL 跑)
mvn -pl ruoyi-admin spring-boot:run -Dspring-boot.run.profiles=dev

# 后端打包 (产出 ruoyi-admin.jar, 用 ry.sh 启停)
mvn package -DskipTests
./ry.sh start | stop | restart | status

# 单测
mvn -pl ruoyi-system test -Dtest=ClassName
mvn -pl ruoyi-system test -Dtest=ClassName#methodName

# 编译检查 (JDK 1.8 字节码 major version 52)
mvn clean compile
javap -v ruoyi-system/target/classes/<class> | grep "major version"   # 验证 52

# 前端启动
cd ruoyi-ui && npm run dev    # 默认端口 1024

# 前端 lint
cd ruoyi-ui && npm run lint

# E2E (Playwright, headless 必须 false, slowMo 200ms — 全局规则)
cd ruoyi-ui && npm run test:e2e
# 单文件
npx playwright test tests/e2e/<spec>.spec.js
```

## 数据库

- **默认 schema**: `diy_manager` (见 `application-druid-dev.yml` 的 `master.url`)
- **用户/密码**: `root` / `bxcq5276` (开发环境)
- **容器化**: `docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=bxcq5276 mysql:8.0`
- **初始化数据**: `sql/` 目录有 `ry_20250414.sql` 等（v3.8.8 baseline），新建模块的 `sys_menu` / `sys_role` / `sys_config` 写入用 Flyway 迁移。

## 配置文件位置

| 文件 | 作用 |
|---|---|
| `pom.xml`（根） | 统一版本号 + 模块声明 |
| `ruoyi-admin/src/main/resources/application.yml` | 主配置 |
| `ruoyi-admin/src/main/resources/application-dev.yml` | dev profile（数据库连接、token 配置、captcha 开关） |
| `ruoyi-admin/src/main/resources/application-druid-dev.yml` | Druid 数据源 + 监控密码 |
| `ruoyi-framework/src/main/java/com/ruoyi/framework/config/SecurityConfig.java` | Spring Security URL 白名单、JWT filter |
| `ruoyi-framework/src/main/java/com/ruoyi/framework/config/ApplicationConfig.java` | `@MapperScan("com.ruoyi.**.mapper")` — 新增 mapper 子包时确保此 pattern 能匹配 |

## 不要做的事

- **不要**把 `OKHttp 3.x / 4.x` 这类有版本兼容问题的库直接声明版本号（jasypt-spring-boot-starter 会传递 3.14.9，需要 exclusion）
- **不要**用 `List.of()` / `Map.of()` / `var` / `record` / `StandardCharsets.UTF_8.toString()` —— Java 1.8 编译目标会失败
- **不要**新加 mapper 写到 `com.ruoyi.system.xxx` 顶级包却不放 `.mapper` 子包——RuoYi 的 `@MapperScan` pattern 不匹配，需要补 `@MapperScan` 或加 `annotationClass=Mapper.class` 限定
- **不要**用 Spring Security 内置 `hasAuthority()` 查 RuoYi 菜单权限——必须 `@ss.hasPermi()`
- **不要**硬编码密钥进 git——RuoYi 用 `ENC(...)` 包裹 Jasypt 加密的密钥存 `sys_config` 表
- **不要**在 E2E 用 `headless: true`（全局规则违反）

---

## 业务子领域（`ruoyi-system`，RuoYi 内置 + myDiyManager 自定义）

`ruoyi-system` 是**业务**模块，不只是「系统管理」。原 RuoYi 内置表（`sys_*`）和 myDiyManager 自定义表（`xm_step_*`、`duty_*`、`approval_*` …）**都**写在这里。

| 子包 | 业务 | 关键实体/Mapper | 对应 Vue view | Flyway 迁移 |
|---|---|---|---|---|
| `controller/system/` + `service/ISysXxxService`（顶层）| 内置 RBAC/系统 | `SysUser` `SysRole` `SysMenu` `SysDept` `SysConfig` `SysDict*` `SysNotice` `SysPost` `SysUserOnline` `SysOperLog` `SysLogininfor` | `ruoyi-ui/src/views/system/`、`monitor/` | `V20240924143800__init_db.sql`（baseline） |
| `controller/` + `service/impl/StepConfigServiceImpl` + `task/MiMotionRunner` | **小米步数扫描器**（核心） | `StepConfig` / `XmStepRunLog`，表 `xm_step_config` / `xm_step_run_log` | `ruoyi-ui/src/views/step/{config,log}/` | `V20240929094200__add_xm_step_table.sql` + `V20240925171000__add_manager_menu.sql` + `V20240926141000__add_step_log_menu.sql` + `V20240926173000__add_step_config_menu.sql` + `V20240930101100__add_xm_step_notice_dict.sql` |
| `service/duty/` + `controller/duty/` | 值日 | `Duty*` 实体 | `ruoyi-ui/src/views/duty/{addressBook,department,group,personnel,roster}/` | （如有） |
| `service/file/` | 文件管理（含 strategy/strategy）| `File*` 实体 | `ruoyi-ui/src/views/photo/` | — |
| `service/reimbursement/` | 报销 | `Reimbursement*` | — | — |
| `service/approval/`（含 `record/`）| 审批 | `Approval*` | — | — |
| `service/notification/` | 通知（含 Bark、邮件）| `Notification*` | — | — |
| `service/api/` | 第三方 API 封装 | `Api*` | — | — |

> 「**内置 vs 自定义**」的快速判别：
> - 表名以 `sys_` / `gen_` 开头 → 内置 RuoYi，**别在 RuoYi 表加自定义字段**（应用 Flyway 迁移）
> - 表名以业务名开头（`xm_step_*`、`duty_*` …）→ myDiyManager 自定义
> - `ruoyi-admin/.../web/controller/system/` + `ruoyi-admin/.../web/controller/monitor/` 是 RuoYi 平台 Controller；`ruoyi-system/.../controller/` 是 myDiyManager 业务 Controller（**没有**走 admin 模块）—— 这是反直觉的一点，codegraph 显示「system 业务 Controller 极少」就是这个原因。

## 步数扫描器（MiMotionRunner）—— 业务 Runner 模式

`ruoyi-system/src/main/java/com/ruoyi/system/task/MiMotionRunner.java` 是这个项目**唯一**业务 Runner，但它演示了 myDiyManager 业务任务的标准接入方式：

**架构**：
```
Vue StepConfigController
       │ @PreAuthorize("step:config:*")
       ▼
StepConfigController → IStepConfigService (system)
       │
       ▼
StepConfigServiceImpl.insertStepConfig()
       │ ① Redis INCR 自增 ID（key = Constants.STEP_CONFIG_REDIS_KEY，初始 500）
       │ ② sysJobService.insertJob() 注册 Quartz cron 任务
       │ ③ Redis Hash 写 xm-step-config（id → JSON 配置）做查询缓存
       │ ④ sysJobService.changeStatus() 立即启动 job
       ▼
quartz 表 sys_job / sys_job_log（V20240924144000__create_qz_job.sql）
       │
       ▼  cron 触发，反射 invokeTarget="miMotionRunner.runStep('<id>')"
       │
MiMotionRunner.runStep(key)
       │ ① ThreadUtil.sleep(5~60s) 错开人机检测
       │ ② Redis hash 读 xm-step-config 拿配置；miss 则回源 DB
       │ ③ AES-CBC/PKCS5 加密 payload（KEY/IV 硬编码！）
       │ ④ POST https://bs.yanwan.store/run4/mi20251001.php（第三方步数接口）
       │ ⑤ POST https://bark.aiyatou.cn/<id> 推 Bark 通知
       │ ⑥ INSERT xm_step_run_log 落库执行结果
```

**修改/添加新业务 Runner 的标准步骤**：
1. `task/XxxRunner.java` 用 `@Component("xxxRunner")` 注册 Bean（Bean 名要等于 `invokeTarget` 前缀）
2. Service 在 `insert/update/changeStatus` 里同步调用 `ISysJobService.insertJob/updateJob/changeStatus` —— **不要**直接操作 Quartz 表
3. invokeTarget 字符串遵循 `beanName.methodName('arg')` 格式（RuoYi 反射约定）
4. jobGroup 用业务名（如 `XMSTEP`）区分
5. 迁移：`sys_menu` 加菜单 + 对应权限字符串（`xmstep:config:list` 这种命名）

## Flyway 工作流

**配置**（`application-druid-dev.yml` 第 62-66 行）：
```yaml
spring.flyway:
  enabled: true
  encoding: utf-8
  locations: classpath:db
  table: flyway_schema_history
```

**当前迁移文件**（`ruoyi-admin/src/main/resources/db/`，按时间顺序）：

| 文件 | 作用 |
|---|---|
| `V20240924143800__init_db.sql` | baseline：sys_dept / sys_user / sys_role / sys_menu / sys_post / sys_dict / sys_config / sys_notice / sys_oper_log / sys_logininfor / gen_table / gen_table_column 等 19 张表 + 种子数据 |
| `V20240924144000__create_qz_job.sql` | Quartz 11 张表（`QRTZ_*`）+ 业务表 `sys_job` / `sys_job_log` |
| `V20240925170100__add_dict.sql` | 字典（`sys_dict_type` / `sys_dict_data`）|
| `V20240925171000__add_manager_menu.sql` | 「步数管理」菜单 + 权限 |
| `V20240926141000__add_step_log_menu.sql` | 「步数日志」菜单 |
| `V20240926173000__add_step_config_menu.sql` | 「步数配置」菜单 |
| `V20240929094200__add_xm_step_table.sql` | 业务表 `xm_step_config` + `xm_step_run_log` |
| `V20240930101100__add_xm_step_notice_dict.sql` | 通知相关字典 |

**新建 Flyway 迁移的步骤**：
1. 文件名必须 `V<YYYYMMDD><HHMMSS>__<name>.sql`，时间戳**严格递增**（冲突时 Flyway 校验失败）
2. SQL 引擎用 MySQL InnoDB，**保留** `engine=innodb` 和 `comment`，方便后续维护
3. 加 `sys_menu` 记录时，权限字符串建议 `业务:实体:操作` 三段式（参考 `step:config:list`）
4. 改已有表结构：写新迁移文件，**不要**回改旧文件（`flyway_schema_history` 已锁定 checksum）
5. 本地开发库被锁住时，**不要**手动 `DROP DATABASE` 后又跑一遍旧 SQL —— 让 Flyway 接管

**`sql/` vs `ruoyi-admin/src/main/resources/db/` 的关系**：
- `sql/ry_20240629.sql` —— v3.8.8 之前的**历史** baseline 快照（不参与 Flyway）
- `sql/quartz.sql` —— 单独导出的 Quartz 脚本（已在 `V20240924144000__create_qz_job.sql` 涵盖，**不要**再单独执行）
- 真正启动时只跑 `db/` 下的 Flyway 迁移；新环境初始化时只需 `CREATE DATABASE diy_manager` + 启动 admin 模块

## 安全 / JWT / 权限详解

**JWT 流程**（`ruoyi-framework/.../web/service/TokenService.java`）：
- 登录：`SysLoginService` 调 `AuthenticationManager` → 拿 `LoginUser` → `TokenService.createToken()` 生成 UUID token + Jwts HS512 签发
- token 头：`Authorization: Bearer <token>`，前缀 `Constants.TOKEN_PREFIX` = `"Bearer "`
- 有效期：`token.expireTime` 默认 **30 分钟**（`application-dev.yml` 第 100 行），**剩余 ≤ 20 分钟**自动续期
- 缓存：Redis 存 `login_tokens:<uuid>` → `LoginUser`，TTL 与 token 一致
- 过滤器：`JwtAuthenticationTokenFilter` 在 `UsernamePasswordAuthenticationFilter` 之前，每请求验签 + 刷新

**自定义权限 SpEL `@ss`**（`ruoyi-framework/.../web/service/PermissionService.java`，Bean 名 `ss`）：
| 表达式 | 校验 |
|---|---|
| `@ss.hasPermi('system:user:list')` | `sys_menu.perms` 包含该串（`Constants.ALL_PERMISSION = "*:*:*"` 视作超管） |
| `@ss.lacksPermi('xxx')` | 取反 |
| `@ss.hasAnyPermi('a,b')` | 任一命中（分隔符 `Constants.PERMISSION_DELIMETER`）|
| `@ss.hasRole('admin')` / `@ss.hasAnyRoles('a,b')` | 用户角色列表命中（`Constants.ROLE_DELIMETER = ","`）|
| `@ss.lacksRole('xxx')` | 角色取反 |

**新增权限的完整流程**（缺一不可）：
1. `sys_menu` 插入菜单 + `perms` 字段（**菜单表驱动权限**，不是角色表）
2. `sys_role_menu` 把菜单绑给角色
3. 登录时 `SysPermissionService` 把命中菜单的 `perms` 塞进 `LoginUser.permissions`，**进 Redis 缓存**（所以改了菜单权限要重新登录或清 token 缓存）
4. Controller 写 `@PreAuthorize("@ss.hasPermi('xxx:xxx:list')")`

## 监控与缓存 API（admin 模块）

`ruoyi-admin/src/main/java/com/ruoyi/web/controller/monitor/` 提供 4 个内置监控端点：
- `ServerController` —— `/monitor/server`：CPU/内存/磁盘/JVM（运行时实时采集）
- `CacheController` —— `/monitor/cache`：Redis 缓存信息（依赖 `RedisCache.getCacheList()`）
- `SysUserOnlineController` —— `/monitor/online`：当前活跃 session（强制下线用 `tokenService.delLoginUser`）
- `SysLogininforController` / `SysOperlogController` —— 登录/操作日志查询

Druid 监控：`/druid/*`，账号 `ruoyi` / `123456`（`application-druid-dev.yml` 配 `statViewServlet.login-*`）

## 前端组织

```
ruoyi-ui/src/
├── api/                    ← 按业务领域拆 JS 模块（不是按页面）
│   ├── login.js menu.js
│   ├── system/{user,role,menu,dept,post,config,notice,dict/{type,data}}.js
│   ├── monitor/ tool/ step/
├── views/                  ← 按页面
│   ├── system/ monitor/ tool/      ← RuoYi 内置
│   ├── step/{config,log}/          ← 步数（业务）
│   ├── duty/{addressBook,department,group,personnel,roster}/ ← 值日
│   ├── photo/ team/                ← 文件/团队
│   └── dashboard/ error/ login.vue register.vue redirect.vue index.vue
├── store/  router/  utils/  components/  layout/  directive/  plugins/
├── permission.js           ← 路由守卫（启动时拉菜单 → 动态挂载）
└── main.js                 ← 入口（含 Vue.prototype 注入 dict/下载/分页等全局方法）
```

**新增业务页面**的标准流程：
1. `api/<area>/<entity>.js` —— axios 封装（参考 `api/system/user.js`，统一 `request` 模块）
2. `views/<area>/<entity>/index.vue` —— Element UI 表格 + 查询表单
3. `router/` 加路由（动态菜单的话可省略，`sys_menu` 表里加菜单项会自动挂载）
4. 权限点用 `v-permis="['<area>:<entity>:list']"` 指令控制按钮显隐（`src/directive/` 实现的全局指令）
