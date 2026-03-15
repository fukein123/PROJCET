# Skills 安装记录

更新时间：2026-03-14

## 1. 当前策略

本项目继续执行“少量高质量、按需安装、优先解决实际问题”的原则。

本轮没有继续堆积与当前仓库无关的技能，而是优先安装对以下场景有明确帮助的能力：

- 前端设计落地
- 文档检索与技术决策
- 数据分析与报告
- 安全治理
- 监控告警
- 部署发布

## 2. 本轮新增安装

本轮新增安装 8 个技能，均安装到 `C:\Users\lenovo\.codex\skills\`：

| Skill | 用途 | 与本项目的关系 |
| --- | --- | --- |
| `openai-docs` | 官方文档检索与 API/模型能力确认 | 便于后续对接 OpenAI 能力或使用最新官方文档 |
| `figma-implement-design` | 将设计稿更稳定地落地为前端实现 | 适合后续统一管理端、门户端和志愿者端视觉规范 |
| `jupyter-notebook` | 数据分析、统计、报表、实验性校验 | 可用于统计数据、日志分析、回归数据排查 |
| `security-ownership-map` | 安全责任与边界梳理 | 适合后续梳理上传、鉴权、日志、敏感数据责任边界 |
| `sentry` | 错误监控、告警、问题归因 | 适合上线后前后端异常采集与告警治理 |
| `render-deploy` | Render 部署支持 | 适合后续快速建立公开预览环境 |
| `vercel-deploy` | Vercel 部署支持 | 适合前端静态站点或预览环境发布 |
| `netlify-deploy` | Netlify 部署支持 | 适合前端静态构建产物的备用部署出口 |

## 3. 当前已安装且仍然有价值的技能

已有技能中，当前对本项目最直接有价值的包括：

- `playwright`
- `playwright-interactive`
- `screenshot`
- `aspnet-core`
- `doc`
- `pdf`
- `linear`
- `gh-fix-ci`
- `gh-address-comments`
- `security-best-practices`
- `security-threat-model`
- `spreadsheet`
- `imagegen`
- `figma`
- `chatgpt-apps`

说明：

- 其中 `playwright`、`screenshot`、`doc`、`pdf`、`security-*`、`spreadsheet` 对当前仓库最常用。
- `deploy` 类技能暂未在本轮直接使用，但对后续“其它电脑可运行、可部署、可预览”的目标有明确价值。

## 4. 本轮实际使用

本轮实际使用的技能与用途如下：

- `skill-installer`
  - 用途：列出并安装本轮新增技能
- `playwright`
  - 用途：辅助页面结构与视觉问题排查

## 5. 后续使用原则

- 每一轮优先使用当前会话已具备、且与任务直接相关的技能。
- 新增技能必须能够服务当前问题或近期明确计划，不为“也许以后会用”预先堆积。
- 安装后的技能要落实到代码、验证、文档和流程改进，而不是只停留在安装记录。

## 6. 说明

新增技能安装完成后，如需在后续会话中直接出现在可用技能列表中，通常需要重新启动 Codex 会话。
