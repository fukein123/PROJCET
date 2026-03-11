# 社区志愿服务管理系统（CVS）

前后端分离项目：

- **后端**：Spring Boot + MyBatis-Plus + Spring Security + JWT + SpringDoc OpenAPI + Hutool + Lombok + Hibernate Validator + Maven + `@Scheduled`（JDK/Java 21）
- **前端**：Vue 3 + Vite + TypeScript + Element Plus + Axios + Pinia + Vue Router + Lodash + Day.js + ECharts

目录：

- `admin/`：管理端与志愿者端前端（Vue3）
- `backend/`：后端服务（Spring Boot）
- `file/`：统一文档目录（开发/接口/结构/配置/设计等）
- `skills/`：开发辅助 skills（按需下载/更新）

## 本地环境要求

- **JDK**：21
- **Maven**：`E:\MAVEN`（本地仓库 `E:\mvn_repo`）
- **MySQL**：8.0（`localhost:3306`，用户 `root`，密码 `123456`，数据库 `CVS`）
- （可选）**Nginx**：反向代理端口 `70`

## 快速启动

### 1) 初始化数据库

在 MySQL 中执行：

- `file/db/CVS.sql`

### 2) 启动后端

后端工程位于 `backend/`。

- 使用 IDE（IntelliJ IDEA）导入 Maven 工程
- 运行 `com.community.Application`

OpenAPI/Swagger（启动后）：

- `/swagger-ui/index.html`

### 3) 启动前端

前端工程位于 `admin/`。

```bash
cd admin
npm i
npm run dev
```

## GitHub 配置（按你的账号信息）

由于本工作区不会自动修改你的全局/本地 Git 配置，请你在本机执行：

```bash
git config user.name "fukexin123"
git config user.email "3547795196@qq.com"
```

## 文档

所有文档统一在 `file/`：

- `file/DEV_GUIDE.md`：开发指南
- `file/ARCHITECTURE.md`：系统架构与模块说明
- `file/CONFIG.md`：配置说明（MySQL/Nginx/Maven/JDK 等）
- `file/API.md`：接口约定与鉴权说明

