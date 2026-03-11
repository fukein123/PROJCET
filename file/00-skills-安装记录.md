# Skills 安装记录

更新时间：2026-03-11

## 说明

按你的要求先创建了项目内 `skills/` 目录，并从公开仓库下载可用 skills。  
`skill-installer` 自带 Python 脚本在当前环境不可执行（缺少 python），已按其失败回退流程改为 GitHub 仓库直接拉取并拷贝。

## 已安装 skills（项目内）

- `frontend-design`（来自 `anthropics/skills`）
- `remotion-best-practices`（来自 `nad128668/remotion_skills_codex`）
- `canvas-design`（来自 `anthropics/skills`）
- `brand-guidelines`（来自 `anthropics/skills`）
- `webapp-testing`（来自 `anthropics/skills`）
- `pdf`（来自 `anthropics/skills`）
- `security-best-practices`（来自 `openai/skills`）
- `sentry`（来自 `openai/skills`）

## 你给出的推荐名与安装映射

- `frontend_desigon` -> `frontend-design`
- `remotion` -> `remotion-best-practices`
- `UI UX ProMax` -> `canvas-design` + `brand-guidelines`
- `Systematic Debugging` -> `webapp-testing`
- `Error Log Summarizer` -> `sentry`
- `SQL Injector Shield` -> `security-best-practices`
- `pdf` -> `pdf`

以下名称在公开仓库中未找到同名 skill，已使用最接近可替代 skill：

- `UniversalCodeReviewer`
- `TeamStyleEnforcer`
- `Postman x Antigravity`
- `Git Commit Message Pro`
- `Dockerfile Optimizer`

## 操作提示

如果要让 Codex 会话识别新安装 skills，按 `skill-installer` 说明需要重启会话：

`Restart Codex to pick up new skills.`

