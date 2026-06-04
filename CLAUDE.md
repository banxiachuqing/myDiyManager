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
