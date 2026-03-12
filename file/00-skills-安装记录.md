# Skills 安装记录

更新时间：2026-03-12

## 1. 目标

项目内 `skills/` 目录现在只保留和当前研发流程直接相关的核心 skill，避免仓库夹带过多第三方内容，降低维护成本和上下文噪声。

## 2. 当前保留的核心 skill

- `frontend-design`
  - 用途：统一页面视觉方向、提升界面设计质量、避免通用化模板页面。
- `webapp-testing`
  - 用途：启动本地服务并用 Playwright 做页面冒烟、交互验证和排错。
- `pdf`
  - 用途：当需求文档、交付文档以 PDF 形态流转时，做提取、核对和渲染验证。

## 3. 已移除的非核心 skill

- `brand-guidelines`
- `canvas-design`
- `remotion-best-practices`
- `security-best-practices`
- `sentry`

以上 skill 不再随项目仓库分发；如果后续任务确实需要，可在会话环境中按需重新安装。

## 4. 调整原则

- 优先保留和当前项目主链路直接相关的 skill：界面设计、页面验证、文档核对。
- 能通过全局环境或临时安装补充的 skill，不再长期存放在仓库内。
- 项目文档只记录当前有效的核心 skill，避免“目录里有但开发中不用”的冗余状态。
