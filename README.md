# zero2.0 - 社区志愿服务管理系统（CVS）

前后端分离项目，包含：

- `backend`：Spring Boot 3.5 + MyBatis-Plus + JWT + Spring Security + OpenAPI
- `Frontend`：Vue 3 + Element Plus + TypeScript + Pinia + Vue Router + Axios + ECharts
- `skills`：本地开发技能库
- `file`：项目文档（开发、接口、配置、设计、部署）

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

## 文档导航

- 文档目录：`file/`
- 文档索引：`file/README.md`
