# zero2.0

社区志愿服务管理系统，采用前后端分离结构：

- `backend`：Java 21 + Spring Boot 3.5 + Spring Security + JWT + MyBatis-Plus
- `Frontend`：Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router
- `file`：需求、设计、部署、迭代记录等项目文档
- `skills`：项目内保留的核心开发 skill

## 项目结构

```text
zero/
├─ backend/
├─ Frontend/
├─ file/
├─ skills/
└─ README.md
```

## 快速启动

### 1. 初始化数据库

```bash
mysql -uroot -p123456 -e "CREATE DATABASE IF NOT EXISTS CVS DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -uroot -p123456 CVS < backend/src/main/resources/db/schema.sql
mysql -uroot -p123456 CVS < backend/src/main/resources/db/data.sql
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

- 默认端口：`8080`
- OpenAPI：`http://localhost:8080/swagger-ui.html`

### 3. 启动前端

```bash
cd Frontend
npm install
npm run dev
```

- 默认端口：`5173`

## 默认账号

- 管理员：`admin / 123456`
- 志愿者：`volunteer / 123456`

## 开发约定

- 前端只通过 `Frontend/src/api/*` 调用后端 REST 接口。
- 后端统一返回 `ApiResponse`，分页统一使用 `PageResult`。
- 结构治理、功能补足和每轮验证记录统一写入 `file/` 目录。
- 当前本地核心 skill 仅保留 `frontend-design`、`webapp-testing`、`pdf`，其他按需再装。

## 文档索引

- 文档目录：`file/`
- 索引文件：`file/README.md`
- 本轮治理计划：`file/12-阶段治理与迭代计划.md`
