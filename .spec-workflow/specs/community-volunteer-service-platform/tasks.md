# Tasks Document

- [x] 1. 补齐登录角色匹配与登出状态一致性校验
  - File: `backend/src/main/java/com/community/modules/auth/service/AuthService.java`
  - File: `Frontend/src/store/modules/auth.ts`
  - 校验登录时账号角色与所选角色一致，统一登录失败提示；登出时确保本地鉴权状态、角色状态与路由入口同步清空。
  - Purpose: 落实 Requirement 1 的身份与工作台入口边界，避免错角色登录或残留状态。
  - _Leverage: `backend/src/main/java/com/community/common/config/SecurityConfig.java`, `Frontend/src/utils/request.ts`_
  - _Requirements: 1_
  - _Prompt: Implement the task for spec community-volunteer-service-platform, first run spec-workflow-guide to get the workflow guide then implement the task: Role: 全栈工程师（鉴权与会话治理） | Task: 在 AuthService 与前端 auth store 中补齐角色匹配、登出清理与错误提示一致性，满足 Requirement 1 | Restrictions: 不修改现有 token 结构；不引入新的鉴权框架；保持现有 API 路径不变 | _Leverage: `SecurityConfig`, `request.ts` | _Requirements: 1 | Success: 错角色登录被拒绝且提示明确，登出后受保护页面不可访问 | Instructions: 开始实现前将本任务从 `[ ]` 改为 `[-]`；实现并验证后调用 `log-implementation` 记录产物；最后将 `[-]` 改为 `[x]`_

- [x] 2. 为活动分类删除约束补齐可回归测试
  - File: `backend/src/test/java/com/community/modules/activity/ActivityServiceCategoryTest.java`
  - File: `backend/src/main/java/com/community/modules/activity/service/ActivityService.java`
  - 为“被活动引用的分类不可删除”与批量删除场景补测试并修正边界行为。
  - Purpose: 落实 Requirement 2.2 的强约束，避免误删导致数据悬挂。
  - _Leverage: `backend/src/main/java/com/community/common/exception/BusinessException.java`_
  - _Requirements: 2_
  - _Prompt: Implement the task for spec community-volunteer-service-platform, first run spec-workflow-guide to get the workflow guide then implement the task: Role: 后端工程师（业务规则与测试） | Task: 为活动分类单删/批删补齐引用约束测试并修复实现细节，满足 Requirement 2 | Restrictions: 不改动数据库表结构；不绕开现有异常体系；保持已有接口响应格式 | _Leverage: `BusinessException` | _Requirements: 2 | Success: 分类被引用时删除失败且可读报错，未引用分类可正常删除，测试稳定通过 | Instructions: 开始前标记 `[-]`；完成后记录 implementation log；再改为 `[x]`_

- [x] 3. 拆分活动服务中的查询与命令职责（第一步）
  - File: `backend/src/main/java/com/community/modules/activity/service/ActivityQueryService.java`
  - File: `backend/src/main/java/com/community/modules/activity/service/ActivityService.java`
  - 将活动分页、详情、报名记录分页、签到记录分页等查询逻辑下沉至 `ActivityQueryService` 并由 `ActivityService` 委托调用。
  - Purpose: 降低 `ActivityService` 体积，落实 Requirement 6 的可维护性目标。
  - _Leverage: `backend/src/main/java/com/community/common/web/PageResult.java`_
  - _Requirements: 2, 3, 6_
  - _Prompt: Implement the task for spec community-volunteer-service-platform, first run spec-workflow-guide to get the workflow guide then implement the task: Role: 后端架构工程师（服务拆分） | Task: 新增 ActivityQueryService 并迁移查询职责，ActivityService 保持门面层，覆盖 Requirement 2/3/6 | Restrictions: 不改变控制器对外接口签名；不改变分页参数语义；每个迁移点都保持行为一致 | _Leverage: `PageResult` | _Requirements: 2,3,6 | Success: 查询职责从 ActivityService 分离，编译通过，接口行为一致 | Instructions: 启动任务先标 `[-]`；落日志后改 `[x]`_

- [x] 4. 拆分活动服务中的命令职责（第二步）
  - File: `backend/src/main/java/com/community/modules/activity/service/ActivityCommandService.java`
  - File: `backend/src/main/java/com/community/modules/activity/service/ActivityService.java`
  - 将活动创建更新删除、报名审核、签到签退等写操作逻辑迁移至 `ActivityCommandService`。
  - Purpose: 持续收敛大体量服务，提升规则可测试性与变更可控性。
  - _Leverage: `backend/src/main/java/com/community/modules/activity/dto/*.java`_
  - _Requirements: 2, 3, 6_
  - _Prompt: Implement the task for spec community-volunteer-service-platform, first run spec-workflow-guide to get the workflow guide then implement the task: Role: 后端工程师（命令模型） | Task: 新增 ActivityCommandService 承担写路径业务规则，ActivityService 作为协调层，落实 Requirement 2/3/6 | Restrictions: 不新增对外 API；保持事务边界清晰；不在 Controller 写业务细节 | _Leverage: 活动模块 DTO 与现有 Service 方法 | _Requirements: 2,3,6 | Success: 写路径职责迁移完成且业务行为不回退，编译通过 | Instructions: 先标 `[-]`，完工并 log 后标 `[x]`_

- [x] 5. 细化内容模块的论坛审核能力边界
  - File: `backend/src/main/java/com/community/modules/content/service/ForumModerationService.java`
  - File: `backend/src/main/java/com/community/modules/content/service/ContentService.java`
  - 抽离帖子审核状态流转（待审/通过/拒绝）和审核意见写入逻辑，统一校验状态迁移合法性。
  - Purpose: 落实 Requirement 4.4，并为后续审核扩展留接口。
  - _Leverage: `backend/src/main/java/com/community/modules/content/entity/ForumPost.java`_
  - _Requirements: 4, 6_
  - _Prompt: Implement the task for spec community-volunteer-service-platform, first run spec-workflow-guide to get the workflow guide then implement the task: Role: 后端工程师（内容审核） | Task: 新增 ForumModerationService 并迁移审核状态机逻辑，满足 Requirement 4/6 | Restrictions: 不修改论坛帖子表字段；不改变审核 API 路由；保留现有响应结构 | _Leverage: `ForumPost` 实体与 ContentService 现有审核入口 | _Requirements: 4,6 | Success: 审核状态迁移受控、非法迁移被阻断且提示清晰 | Instructions: 开始前 `[-]`，记录日志后 `[x]`_

- [x] 6. 统一管理端列表页剩余危险操作链路
  - File: `Frontend/src/views/audit/ApplicationAuditView.vue`
  - File: `Frontend/src/views/volunteer/VolunteerActivityCenterView.vue`
  - 将页面内仍存在的手写确认/刷新流程统一接入 `runConfirmedAction` 与共享反馈风格。
  - Purpose: 落实 Requirement 6.2，减少重复交互脚本并统一体验。
  - _Leverage: `Frontend/src/utils/confirmed-action.ts`, `Frontend/src/composables/useTable.ts`_
  - _Requirements: 3, 6_
  - _Prompt: Implement the task for spec community-volunteer-service-platform, first run spec-workflow-guide to get the workflow guide then implement the task: Role: 前端工程师（复用治理） | Task: 将审核页与活动中心页危险操作确认与刷新链路统一为共享工具，满足 Requirement 3/6 | Restrictions: 不重写页面结构；不改变接口调用参数；保持现有中文文案语义 | _Leverage: `runConfirmedAction`, `useTable` | _Requirements: 3,6 | Success: 页面危险操作链路统一，代码重复减少且行为一致 | Instructions: 标记 `[-]` -> 实现+验证+log -> `[x]`_

- [x] 7. 为内容管理分页与按标签加载补前端回归测试
  - File: `Frontend/src/views/content/ContentManageView.vue`
  - File: `Frontend/src/views/content/__tests__/ContentManageView.spec.ts`
  - 覆盖“首屏只加载当前标签”和“模块级刷新不触发全量请求”场景。
  - Purpose: 固化 Requirement 2.5 与 Performance 要求，避免性能回退。
  - _Leverage: `Frontend/src/api/content.ts`_
  - _Requirements: 2, 4, 6_
  - _Prompt: Implement the task for spec community-volunteer-service-platform, first run spec-workflow-guide to get the workflow guide then implement the task: Role: 前端测试工程师 | Task: 为 ContentManageView 补齐按标签加载与模块刷新测试，覆盖 Requirement 2/4/6 性能约束 | Restrictions: 不引入新的测试框架；测试不依赖真实后端；保持现有组件行为不变 | _Leverage: `content.ts` API 封装与现有测试基建 | _Requirements: 2,4,6 | Success: 关键性能场景有自动化回归覆盖且稳定通过 | Instructions: 任务开始改 `[-]`，日志记录后改 `[x]`_

- [x] 8. 扩展评论分页与目标过滤的后端集成测试
  - File: `backend/src/test/java/com/community/modules/content/ContentControllerCommentIT.java`
  - File: `backend/src/main/java/com/community/modules/content/controller/ContentController.java`
  - 补齐评论分页在 `targetType + targetId` 组合过滤下的正确性与鉴权边界验证。
  - Purpose: 落实 Requirement 4.5 和 Reliability 要求。
  - _Leverage: `backend/src/main/java/com/community/common/exception/GlobalExceptionHandler.java`_
  - _Requirements: 4_
  - _Prompt: Implement the task for spec community-volunteer-service-platform, first run spec-workflow-guide to get the workflow guide then implement the task: Role: 后端测试工程师（集成测试） | Task: 扩展评论分页过滤与鉴权边界测试，必要时微调控制器参数校验，满足 Requirement 4 | Restrictions: 不改变现有接口 URL；不引入破坏性字段变更；保持统一错误响应格式 | _Leverage: 全局异常处理与现有内容控制器 | _Requirements: 4 | Success: 评论分页过滤与权限边界可回归验证，测试稳定 | Instructions: `[-]` -> 实现并 log -> `[x]`_

- [x] 9. 规范周榜重建与定时任务运行可观测性
  - File: `backend/src/main/java/com/community/modules/schedule/ActivityScheduleTask.java`
  - File: `backend/src/main/java/com/community/modules/dashboard/service/VolunteerWeeklyStatsService.java`
  - 增加关键日志字段与失败场景保护，确保手动重建与定时重建均可追踪。
  - Purpose: 落实 Requirement 5.3/5.4 的可运营性。
  - _Leverage: `backend/src/main/java/com/community/modules/dashboard/controller/DashboardController.java`_
  - _Requirements: 5_
  - _Prompt: Implement the task for spec community-volunteer-service-platform, first run spec-workflow-guide to get the workflow guide then implement the task: Role: 后端工程师（调度与统计） | Task: 增强周榜重建与定时任务日志与错误保护，满足 Requirement 5 | Restrictions: 不改变现有调度 cron；不改变重建 API 合约；保持统计口径不变 | _Leverage: `DashboardController` 与 VolunteerWeeklyStatsService | _Requirements: 5 | Success: 调度与重建路径可观测、失败可诊断、行为稳定 | Instructions: 开始标 `[-]`，日志记录后标 `[x]`_

- [x] 10. 扩展自动化冒烟到审核与签到签退链路
  - File: `Frontend/package.json`
  - File: `scripts/smoke-test.mjs`
  - 在现有登录/报名/评论/退出基础上，新增管理员审核与志愿者签到签退场景。
  - Purpose: 落实 Requirement 3 与 Requirement 5 的端到端可验证性。
  - _Leverage: `file/11-自动化冒烟脚本说明.md`_
  - _Requirements: 3, 5, 6_
  - _Prompt: Implement the task for spec community-volunteer-service-platform, first run spec-workflow-guide to get the workflow guide then implement the task: Role: 测试自动化工程师 | Task: 扩展 smoke 脚本覆盖审核与签到签退流程，满足 Requirement 3/5/6 | Restrictions: 不硬编码环境私有地址；不依赖人工交互；保持脚本可在当前项目命令下直接运行 | _Leverage: 现有 smoke 脚本与脚本说明文档 | _Requirements: 3,5,6 | Success: 新增场景可稳定执行并输出明确结果 | Instructions: 开始前改 `[-]`，完成后 log 并改 `[x]`_

- [x] 11. 建立需求-接口映射清单并更新文档
  - File: `file/02-接口文档.md`
  - File: `file/08-新增接口与数据结构说明.md`
  - 为 Requirement 1-6 增加“需求条目 -> API/页面/服务”映射，标明已覆盖和待补齐项。
  - Purpose: 落实 Requirement 6.4，降低“实现与文档脱节”风险。
  - _Leverage: `.spec-workflow/specs/community-volunteer-service-platform/requirements.md`, `.spec-workflow/specs/community-volunteer-service-platform/design.md`_
  - _Requirements: 6_
  - _Prompt: Implement the task for spec community-volunteer-service-platform, first run spec-workflow-guide to get the workflow guide then implement the task: Role: 技术文档工程师 | Task: 更新接口与数据文档，建立需求映射矩阵并标注覆盖状态，满足 Requirement 6 | Restrictions: 不虚构未实现接口；不删除已有有效文档段落；保持中文术语一致 | _Leverage: requirements/design 文档与现有接口文档 | _Requirements: 6 | Success: 需求到接口映射清晰、可审计、可追踪 | Instructions: 改 `[-]` 后实施，记录日志后改 `[x]`_

- [x] 12. 完成本轮规格闭环验证并刷新治理文档
  - File: `file/06-平台完善记录.md`
  - File: `file/12-阶段治理与迭代计划.md`
  - File: `file/13-开发约束与计划跟踪.md`
  - 汇总 tasks 实施与验证结果，更新已完成/未完成项与下一轮门禁状态。
  - Purpose: 落实 Requirement 6.4/6.5，确保治理文档与实际进度一致。
  - _Leverage: `.approval-workflow/`, `.spec-workflow/specs/community-volunteer-service-platform/tasks.md`_
  - _Requirements: 6_
  - _Prompt: Implement the task for spec community-volunteer-service-platform, first run spec-workflow-guide to get the workflow guide then implement the task: Role: 交付治理工程师 | Task: 在任务实施完成后刷新治理文档与阶段状态，满足 Requirement 6 | Restrictions: 不回滚既有真实记录；不省略失败或阻塞信息；结论需与审批记录一致 | _Leverage: `.approval-workflow`, `tasks.md` | _Requirements: 6 | Success: 治理文档与真实实施状态一致，下一轮可直接接续执行 | Instructions: 执行前标 `[-]`，完成并 log 后标 `[x]`_

