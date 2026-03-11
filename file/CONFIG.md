# 配置说明

## MySQL

- 地址：`localhost`
- 端口：`3306`
- 用户：`root`
- 密码：`123456`
- 数据库：`CVS`

初始化脚本：`file/db/CVS.sql`

## Maven

你本机 Maven：

- Maven 目录：`E:\MAVEN`
- 本地仓库：`E:\mvn_repo`

如需在 IDEA 中配置：Settings → Build Tools → Maven。

## JDK / Java

- JDK：21
- 后端基于 Spring Boot（Java 21）

## Nginx（可选）

要求：反向代理端口为 `70`。

示例（按需改动上游端口）：

```nginx
server {
  listen 70;

  location /api/ {
    proxy_pass http://127.0.0.1:8080/;
  }

  location / {
    proxy_pass http://127.0.0.1:5173/;
  }
}
```

