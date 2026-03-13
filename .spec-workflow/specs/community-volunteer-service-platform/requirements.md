# Requirements Document

## Introduction

社区志愿服务平台旨在构建一个面向社区管理者、志愿者和公众访客的前后端分离系统，统一承载志愿活动组织、报名审核、签到签退、资讯公告、论坛互动、统计看板与项目治理能力。  
本规格以当前仓库中的产品文档、接口文档、结构文档和已实现能力为基础，恢复 `.spec-workflow` 正式规格实例，作为后续设计、任务拆解和迭代实施的唯一需求基线。

## Alignment with Product Vision

当前仓库尚未建立 `.spec-workflow/steering/product.md`，因此本节以现有项目说明为准绳，对齐以下产品目标：

- 面向三类角色建立统一平台：管理员负责治理与运营，志愿者负责参与与反馈，公众负责浏览与了解社区服务内容。
- 坚持前后端分离：前端专注体验与流程组织，后端提供统一、稳定、可治理的业务接口。
- 在保持项目可运行的前提下，持续推进结构治理、重复代码收敛、文档同步和验证闭环，确保项目能够长期演进。
- 以社区志愿服务业务为核心，而不是偏离到无关功能或历史残留模板资产。

## Requirements

### Requirement 1

**User Story:** As an administrator or volunteer, I want a role-based authentication and workspace entry flow, so that I can securely enter the correct workspace and perform actions allowed for my role.

#### Acceptance Criteria

1. WHEN a user submits login credentials with a selected role THEN the system SHALL validate the credentials and role match before issuing a login token.
2. IF a user is authenticated as `ADMIN` THEN the system SHALL route the user to administrator workspaces and restrict volunteer-only actions.
3. IF a user is authenticated as `VOLUNTEER` THEN the system SHALL route the user to volunteer workspaces and restrict administrator-only actions.
4. WHEN a user logs out THEN the system SHALL clear local authentication state and return the user to the public portal.
5. IF an unauthenticated user attempts a protected action such as registration-dependent activity signup, profile editing, favorites, or forum posting THEN the system SHALL block the action and require authentication.

### Requirement 2

**User Story:** As an administrator, I want to manage volunteer activities across their full lifecycle, so that community service plans can be published, audited, and completed in an organized way.

#### Acceptance Criteria

1. WHEN an administrator creates or edits an activity THEN the system SHALL support category, title, content, description, cover image, address, volunteer quota, target count, start time, end time, and publish status.
2. WHEN an administrator manages activity categories THEN the system SHALL support create, update, single delete, and batch delete while preventing deletion of categories still referenced by activities.
3. WHEN an administrator deletes an activity THEN the system SHALL remove or cascade related signup, check-in, and favorite records according to business rules.
4. WHEN an activity reaches its end time THEN the system SHALL support lifecycle transition to an ended state through scheduled or equivalent governance logic.
5. WHEN an administrator accesses activity management views THEN the system SHALL provide paged list queries rather than requiring full dataset loading.

### Requirement 3

**User Story:** As a volunteer, I want to discover, join, attend, and review activities, so that I can complete the full participation journey within one platform.

#### Acceptance Criteria

1. WHEN a volunteer browses activities from the portal or volunteer workspace THEN the system SHALL provide activity list and detail views with readable schedule, location, and description information.
2. IF a volunteer has not passed required identity or certification checks THEN the system SHALL block activity signup and explain why the action is unavailable.
3. WHEN a volunteer signs up for an activity THEN the system SHALL prevent duplicate submissions and enforce volunteer quota constraints when configured.
4. WHEN an administrator audits a volunteer application THEN the system SHALL support approval or rejection and retain audit status and rejection reason when applicable.
5. WHEN a volunteer performs sign-in or sign-out THEN the system SHALL validate timing constraints against the activity schedule and record attendance status.
6. WHEN a volunteer views personal participation history THEN the system SHALL provide paged application and check-record lists with status, related activity information, and readable service duration.
7. WHEN a volunteer manages favorites THEN the system SHALL support create, read, update, delete, and batch delete for saved activities.

### Requirement 4

**User Story:** As a portal visitor, volunteer, or administrator, I want a unified content and community interaction system, so that community information distribution and discussion can happen in one place.

#### Acceptance Criteria

1. WHEN a visitor opens the portal home THEN the system SHALL provide access to public-facing activity, dynamic news, notice, and forum content.
2. WHEN an administrator manages dynamics, notices, banners, forum categories, forum posts, or comments THEN the system SHALL support the corresponding create, update, single delete, and batch delete governance actions required by each module.
3. WHEN a volunteer or authorized user posts forum content or comments THEN the system SHALL persist the content through backend APIs rather than front-end-only placeholder behavior.
4. IF a forum post requires moderation THEN the system SHALL support pending, approved, and rejected states with audit feedback.
5. WHEN comments are listed in management or portal contexts THEN the system SHALL support paged retrieval by target type and target identifier.

### Requirement 5

**User Story:** As an administrator or volunteer, I want dashboards and periodic statistics, so that the platform can reflect operational state and volunteer contribution trends.

#### Acceptance Criteria

1. WHEN an administrator opens the dashboard THEN the system SHALL provide management-oriented summary indicators for core platform operations.
2. WHEN a user opens the weekly ranking view THEN the system SHALL provide weekly volunteer statistics in a readable ranking format.
3. WHEN an administrator triggers a ranking rebuild or scheduled statistics generation THEN the system SHALL regenerate weekly ranking data without requiring manual database edits.
4. WHEN periodic maintenance logic runs THEN the system SHALL support governance tasks such as expired activity cleanup and weekly statistic generation.

### Requirement 6

**User Story:** As a project maintainer, I want the codebase, repository structure, and delivery process to stay governed, so that iteration can continue without structural drift or undocumented behavior.

#### Acceptance Criteria

1. WHEN the project evolves THEN the system SHALL preserve a front-end and back-end separated architecture, with the front end calling backend REST APIs rather than coupling to persistence logic.
2. WHEN list-heavy pages are implemented or refactored THEN the system SHALL prefer shared pagination, display, upload, selection, and confirmation utilities over repeated page-local implementations.
3. WHEN repository cleanup is performed THEN the project SHALL remove clearly unrelated, duplicated, or obsolete assets that are not part of the main delivery chain.
4. WHEN a development round finishes THEN the project SHALL update relevant documentation in `file/` to reflect actual structure, constraints, completed work, and verification results.
5. WHEN a significant refactor or feature iteration is delivered THEN the project SHALL include runnable verification such as front-end build, backend compile, and browser smoke flow where applicable.

## Non-Functional Requirements

### Code Architecture and Modularity
- **Single Responsibility Principle**: Each file should serve one focused domain concern; large page or service files should be progressively decomposed when repeated responsibilities emerge.
- **Modular Design**: Shared capabilities such as pagination, selection state, status display, upload validation, and confirmation flows should live in reusable composables or utilities.
- **Dependency Management**: Front-end modules shall depend on API wrappers, composables, stores, and utilities through clear layers; back-end controllers shall delegate business logic to services.
- **Clear Interfaces**: Backend responses shall remain consistent through `ApiResponse` and `PageResult`, and front-end API modules shall provide explicit typed entry points.

### Performance
- List-oriented admin and volunteer views SHALL use paged retrieval instead of loading full datasets by default.
- Heavy management pages SHOULD avoid unnecessary all-module refreshes when only one submodule changed.
- Front-end build output SHALL remain production-buildable through the standard `npm run build` pipeline.

### Security
- Authentication SHALL use token-based identity with role-aware authorization boundaries.
- Protected management actions SHALL be restricted to authorized roles.
- Upload flows SHALL validate file types and size constraints before and during backend processing.
- User credentials and identity-sensitive operations SHALL be handled through backend validation, not trusted solely from client-side state.

### Reliability
- The project SHALL remain compilable and buildable through standard backend and frontend commands.
- Validation and business-rule failures SHALL return readable, consistent API errors rather than silent failures.
- Activity signup, moderation, and favorite operations SHALL behave idempotently where repeated user actions are plausible.

### Usability
- The user-facing interface SHALL remain Chinese-first and consistent across administrator, volunteer, and portal experiences.
- Navigation and page structure SHALL clearly distinguish administrator and volunteer workspaces while preserving a unified platform identity.
- Core workflows such as login, signup, comment publishing, and logout SHOULD be verifiable through repeatable smoke flows.
