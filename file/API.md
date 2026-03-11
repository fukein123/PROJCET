# 接口约定（草案）

## Base URL

- 开发：`/api`

## 通用响应体

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

## 鉴权

- 请求头：`Authorization: Bearer <token>`

## 认证接口

- `POST /api/auth/login`
  - 入参：账号、密码、登录类型（管理员/志愿者）、验证码（预留）
  - 出参：token、用户基础信息、角色

- `GET /api/auth/me`
  - 出参：当前登录用户信息

后续模块接口将按领域在 `backend` 中逐步补齐并自动生成 OpenAPI 文档。

