# 开发指南

## 仓库结构

- `admin/`：Vue3 + Element Plus（管理端/志愿者端）
- `backend/`：Spring Boot（REST API）
- `file/`：统一文档目录
- `skills/`：开发辅助 skills（可下载更新）

## 运行依赖

- JDK 21
- Maven：`E:\MAVEN`，本地仓库 `E:\mvn_repo`
- MySQL 8.0：`localhost:3306`（root / 123456），数据库 `CVS`

## 启动顺序

1. 执行 `file/db/CVS.sql` 初始化数据库
2. 启动后端 `backend/`
3. 启动前端 `admin/`

## 端口与反向代理（可选）

建议本地开发：

- 后端：`8080`
- 前端：`5173`

如需 Nginx 统一入口（监听端口 70），参考 `file/CONFIG.md`。

