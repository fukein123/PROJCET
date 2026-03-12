# `.spec-workflow` 中文翻译与作用说明

更新时间：2026-03-12

## 1. `.spec-workflow` 是什么
`.spec-workflow` 是一套“规格先行”的研发文档流程，用来把需求从想法逐步落地到可执行任务。核心价值：

- 先明确产品目标，再写需求，再做设计，最后拆任务。
- 用统一模板减少沟通偏差，避免边做边改方向。
- 让产品、开发、测试、评审都基于同一份规格协作。

## 2. 当前目录说明

- `templates/`：系统默认模板目录。
- `user-templates/`：项目自定义模板目录，可覆盖默认模板。

> 本仓库当前的 `.spec-workflow` 重点是模板体系，适合作为“前后端分离项目”的需求与设计标准化入口。

## 3. 模板中文释义

### 3.1 `product-template.md`
用于定义产品层面的“为什么做”。主要字段可理解为：

- `Product Purpose`：产品目标与价值。
- `Target Users`：目标用户。
- `Key Features`：核心功能。
- `Business Objectives`：业务目标。
- `Success Metrics`：成功指标。

### 3.2 `requirements-template.md`
用于定义“要做什么”，以用户故事 + 验收标准为核心：

- `User Story`：用户故事。
- `Acceptance Criteria`：验收标准（WHEN/IF/THEN）。
- `Non-Functional Requirements`：非功能需求（性能、安全、可用性等）。

### 3.3 `design-template.md`
用于定义“怎么做”：

- `Overview`：设计概览。
- `Architecture`：架构方案。
- `Components and Interfaces`：组件与接口。
- `Data Models`：数据模型。
- `Code Reuse Analysis`：复用分析。

### 3.4 `tasks-template.md`
用于将设计拆分为可执行任务：

- 每条任务都应包含：目标文件、目的、复用点、对应需求编号、执行提示。
- 任务状态通常用 `[ ] / [-] / [x]` 标记。

### 3.5 `tech-template.md`
用于沉淀技术栈与约束：

- 核心技术、环境要求、部署方式、关键技术决策、已知限制。

### 3.6 `structure-template.md`
用于规范目录和代码组织：

- 目录组织、命名规则、模块边界、代码规模建议、文档标准。

## 4. 在本项目中的推荐使用方式

1. 先写 `product`：明确“社区志愿服务平台”的业务目标与角色边界。
2. 再写 `requirements`：按管理员端/志愿者端拆验收条款。
3. 再写 `design`：明确前后端接口、数据库结构、权限边界。
4. 最后写 `tasks`：拆成页面、接口、数据改造、测试和文档任务。

## 5. 为什么对当前项目有用

当前项目是“社区志愿服务平台 + 前后端分离”，`.spec-workflow` 能帮助：

- 避免前端先改 UI、后端再补接口导致反复返工。
- 在管理员端/志愿者端差异化需求下保持一致的规格来源。
- 让后续迭代（如打卡规则、收藏能力、批量管理）可追踪、可审计、可复盘。
