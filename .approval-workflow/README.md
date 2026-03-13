# 本地审批工具

当前项目的唯一审批链路为“聊天回复 + 本地落档”。

## 目标

- 保留“先写文档、再审批、后继续”的门禁思想
- 把审批记录留在仓库本地
- 允许用户直接通过聊天文本批准

## 目录结构

```text
.approval-workflow/
├─ README.md
├─ requests/      # 运行时审批记录
└─ archive/       # 已归档审批记录
```

运行时记录默认不提交 Git，由脚本自动创建。

## 脚本

脚本位置：`scripts/local-approval.ps1`

支持动作：

- `request`
- `status`
- `list`
- `approve`
- `needs-revision`
- `delete`

## 典型流程

1. 创建需求 / 设计 / 任务文档
2. 运行 `request` 创建审批记录
3. 用户在聊天中回复：
   - `通过`
   - `需修改：<具体意见>`
4. 助手根据用户消息运行：
   - `approve`
   - `needs-revision`
5. 审批通过后继续下一阶段

## 示例命令

```powershell
pwsh .\scripts\local-approval.ps1 request `
  -Title "Approve requirements" `
  -FilePath ".spec-workflow/specs/community-volunteer-service-platform/requirements.md" `
  -Category spec `
  -CategoryName community-volunteer-service-platform
```

```powershell
pwsh .\scripts\local-approval.ps1 status -Id <approval-id>
pwsh .\scripts\local-approval.ps1 approve -Id <approval-id> -Comment "用户在聊天中回复：通过"
pwsh .\scripts\local-approval.ps1 needs-revision -Id <approval-id> -Comment "用户在聊天中回复：需修改：补充xxx"
pwsh .\scripts\local-approval.ps1 delete -Id <approval-id>
```
