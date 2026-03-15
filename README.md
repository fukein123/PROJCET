# zero2.0

社区志愿服务管理平台，采用前后端分离结构，围绕管理员后台、登录/注册、门户首页与志愿者自助能力展开。

## 技术栈

- `backend`: Java 21, Spring Boot 3.5, Spring Security, JWT, MyBatis-Plus, Flyway
- `Frontend`: Vue 3, TypeScript, Vite, Element Plus, Pinia, Vue Router
- `database`: MySQL 8，使用 Flyway 维护结构和参考数据

## 目录说明

```text
zero/
├─ backend/                      # 后端服务与数据库迁移
├─ Frontend/                     # 前端应用
├─ file/                         # 项目文档、阶段记录、开发约束
├─ scripts/                      # 验证、迁移、审批辅助脚本
├─ skills/                       # 仓库内保留的最小技能快照
├─ tmp/                          # 临时验证产物（非核心源码，可按需清理）
└─ README.md
```

## 运行前说明

- 数据库初始化入口已经切换到 Flyway。
- `backend/src/main/resources/db/schema.sql` 与 `backend/src/main/resources/db/data.sql` 仅保留为退役占位说明，不再用于运行期初始化。
- 本地开发默认数据库为 `CVS`，默认连接信息见 [application-dev.yml](/f:/zero/backend/src/main/resources/application-dev.yml)。

## 本地启动

### 1. 创建数据库

```bash
mysql -uroot -p123456 -e "CREATE DATABASE IF NOT EXISTS CVS DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

如果本地需要一次性创建数据库账号与授权，可手工执行：

```bash
mysql -uroot -p123456 < backend/src/main/resources/db/init-database.sql
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

- 默认端口：`8080`
- OpenAPI：`http://localhost:8080/swagger-ui.html`
- 启动时 Flyway 会自动执行 `db/migration/` 下的迁移脚本

### 3. 启动前端

```bash
cd Frontend
npm install
npm run dev
```

- 默认端口：`5173`

## 验证命令

```bash
# 后端
cd backend
mvn -DskipTests compile

# 前端
cd Frontend
npm run test:unit
npm run build
```

## 访问边界

- 游客：可浏览 `/portal/*` 的公开内容与详情页，不允许报名、评论、发帖、兑换等操作。
- 志愿者：登录后进入 `/portal`，自助能力统一收口到 `/portal/me/*`。
- 管理员：登录后直接进入 `/admin/dashboard`，禁止进入门户主页面业务链路。
- 兼容路由：旧 `/volunteer/*` 仅保留跳转，不再承载新功能。

## 默认账号

- 管理员：`admin / 123456`
- 志愿者：`volunteer / 123456`

## 当前重点文档

- [结构和配置文档](/f:/zero/file/03-结构和配置文档.md)
- [部署文档](/f:/zero/file/05-部署文档.md)
- [平台完善记录](/f:/zero/file/06-平台完善记录.md)
- [开发约束与计划跟踪](/f:/zero/file/13-开发约束与计划跟踪.md)
- [下一阶段功能补全开发计划](/f:/zero/file/20-下一阶段功能补全开发计划.md)
