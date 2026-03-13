<!--
Sync Impact Report
- Version change: 1.2.0 -> 1.3.0
- Modified principles:
  - Principle 3 -> III. Skill-First Delivery (NON-NEGOTIABLE)
  - Principle 5 -> V. Quality, Test Standards, UX Consistency, and Observability by Default
- Added sections:
  - VI. Domain Focus, Incremental Delivery, and Stakeholder Confirmation
- Removed sections:
  - None
- Templates requiring updates:
  - None
- Follow-up TODOs:
  - None
-->

# Community Volunteer Service Platform Constitution

## Core Principles

### I. Frontend-Backend Separation First
The system MUST be developed as a strict frontend-backend separated architecture. Frontend and backend modules MUST keep clear boundaries for responsibilities, interfaces, and deployment. Any feature proposal that couples UI rendering logic with backend business logic in the same runtime layer MUST be rejected unless an explicit exception is approved in writing.
Rationale: Clear separation reduces change risk, supports independent scaling, and keeps delivery velocity stable.

### II. Latest Stable Technology Baseline
New implementation work MUST use the latest stable major/minor versions that are production-ready at decision time, unless compatibility constraints are documented in the plan. "Latest" means stable release, not preview/nightly. If the team pins to a non-latest version, the plan MUST include explicit reason, impact, and upgrade path.
Rationale: This keeps security posture and ecosystem support current while avoiding experimental instability.

### III. Skill-First Delivery (NON-NEGOTIABLE)
Before custom implementation, contributors MUST evaluate existing local/project skills and apply the smallest sufficient set. Skill download/install is allowed when needed, but MUST follow "quality over quantity": only add skills with direct feature impact, proven utility, and clear ownership. Bulk or speculative skill installation is prohibited. Preferred default strategy for this repository is to keep the active skill set minimal and high-signal, using browser automation or document-oriented skills only when the task clearly benefits from them.
Rationale: Reuse improves consistency and speed; controlled installation avoids tool sprawl and maintenance debt.

### IV. Spec-Workflow Governance Gates
All feature work MUST follow the spec workflow sequence: Requirements -> Design -> Tasks -> Implementation. No implementation starts before required upstream artifacts are complete and approved. For this repository, an approval is valid only when the user explicitly replies in chat with `通过` or `需修改：...`, and the assistant records that outcome through `scripts/local-approval.ps1` under `.approval-workflow/`. If approvals are pending, downstream phases MUST remain blocked and documentation MUST reflect that state.
Rationale: Stage gates prevent scope drift and keep engineering decisions auditable.

### V. Quality, Test Standards, UX Consistency, and Observability by Default
Every feature MUST enforce code quality rules, explicit testing standards, user experience consistency criteria, and operational visibility at design/task level. TDD is mandatory: write tests first, confirm failing tests, then implement, then refactor with tests green. Plans/tasks MUST define verification strategy, acceptance coverage, consistency checks for key user flows, error handling, and logging/monitoring intent. Features that cannot be validated for quality, test coverage, and UX consistency in production-like conditions MUST not be marked complete.
Rationale: Stable delivery requires measurable quality and consistent user experience, not post-hoc fixes.

### VI. Domain Focus, Incremental Delivery, and Stakeholder Confirmation
All work MUST stay centered on the community volunteer service management domain. Delivery MUST proceed incrementally: one requirement slice, one feature slice, or one governance slice at a time, with each slice implemented, validated, documented, and reported before the next begins. Contributors MUST preserve a unified system design language while maintaining clear role-based functional separation across admin, volunteer, and portal areas. Progress updates, current direction, and completion status MUST be communicated promptly, and the next major implementation step MUST wait for user confirmation when the user requests staged confirmation.
Rationale: Domain focus prevents scope drift, incremental delivery keeps risk bounded, and explicit confirmation keeps implementation aligned with stakeholder intent.

## Additional Technical Constraints

- Project architecture MUST remain frontend-backend separated with independent build/run pipelines.
- Frontend files and backend files MUST remain strictly separated; business rules, persistence logic, and API orchestration MUST not leak into frontend modules.
- Feature implementation MUST be anchored to the approved requirements/design artifacts and delivered one slice at a time.
- The product domain MUST remain community volunteer service management; unrelated feature expansion is out of bounds unless newly approved.
- API contracts between frontend and backend MUST be explicitly documented before implementation tasks are approved.
- Technology selection MUST prefer stable ecosystem tooling with active maintenance and documented migration paths.
- The current project framework and established UI language MUST be preserved; new work should extend the existing structure rather than replace it.
- Code quality baseline MUST include naming consistency, readable structure, and maintainability checks at review time.
- Continuous codebase cleanup is required: duplicate logic, dead code, invalid structures, and unnecessary files SHOULD be removed as implementation proceeds.
- UX consistency baseline MUST include consistent interaction patterns, feedback states, and terminology across frontend modules.
- Documentation MUST be updated in the same development round as code or governance changes, and the repository structure MUST be pruned before it becomes bloated.
- Progress reporting MUST include current status, next direction, and outstanding blockers whenever work spans multiple steps.
- Skill usage policy:
  - Prefer existing repository or workspace skills first.
  - Install new skills only when they remove concrete delivery risk or significant repetitive effort.
  - Keep installed skills minimal and periodically prune unused skills.

## Delivery Workflow and Quality Gates

1. Specification quality gate:
   - Spec MUST define user scenarios, measurable success criteria, and edge cases.
2. Planning quality gate:
   - Plan MUST pass constitution checks, including separation model, technology baseline, skill strategy, and TDD/test standards.
3. Task quality gate:
   - Tasks MUST map back to user stories/requirements, include validation steps, and preserve UX consistency criteria.
4. Implementation quality gate:
   - No phase skipping. If required chat approval has not been written to `.approval-workflow/`, stop at the approved phase. Implementation MUST follow test-first TDD sequence.
5. Review quality gate:
   - Reviewers MUST check constitutional compliance, code quality, test evidence, and UX consistency evidence before accepting changes.
6. Incremental handoff gate:
   - When the user requests phased confirmation, each completed slice MUST include a progress summary, validation result, documentation status, and the proposed next direction before the next slice begins.

## Governance

- This constitution is the highest-priority engineering policy for this repository's spec workflow.
- Any pull request, spec artifact, or task list violating a MUST rule is non-compliant and MUST be revised.
- Amendments require:
  - documented reason,
  - impact analysis on templates/prompts/workflow,
  - version update following semantic versioning rules.
- Versioning policy:
  - MAJOR: incompatible principle/governance changes.
  - MINOR: new principle/section or materially expanded rule.
  - PATCH: clarification-only wording changes.
- Compliance review is mandatory at plan time and again before implementation handoff.

**Version**: 1.3.0 | **Ratified**: 2026-03-13 | **Last Amended**: 2026-03-13
