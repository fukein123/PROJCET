# DB Migration Notes

## Responsibility Boundaries

- `../init-database.sql`
  - Owner: DBA / environment bootstrap.
  - Purpose: create database, account, and grants only.
  - Execution: manual, one-time, outside application startup.
  - Forbidden: do not use it for table creation, seed data, or rollback of Flyway migrations.
- `B1__baseline_schema.sql`
  - Owner: Flyway runtime migration chain.
  - Purpose: baseline schema for empty/new databases.
  - Execution: applied automatically by Flyway on empty databases.
  - Forbidden: do not execute manually and do not reuse it as a scratch SQL script.
- `V1__baseline_schema.sql`
  - Owner: Flyway validation compatibility.
  - Purpose: preserve checksum/history compatibility for environments that already applied version `1` before the baseline split.
  - Execution: retained unchanged so existing environments can continue to `validate`.
  - Forbidden: do not edit, repurpose, or manually run it on new environments.
- `V2__legacy_schema_alignment.sql`
  - Owner: Flyway runtime migration chain.
  - Purpose: idempotent alignment patch after version `1`, covering legacy non-empty database adoption and post-baseline alignment.
  - Execution: applied automatically by Flyway after `B1` on empty databases, or after `BASELINE(1)` on existing non-empty databases.
  - Forbidden: do not use it as a manual rollback script.
- `R__seed_reference_data.sql`
  - Owner: Flyway repeatable migration chain.
  - Purpose: reference/master data that must be safe to rerun.
  - Execution: applied automatically by Flyway whenever the file checksum changes.
  - Forbidden: do not place environment-specific credentials, one-off repair SQL, or transactional business data here.
- `../schema.sql` and `../data.sql`
  - Owner: deprecation placeholders.
  - Purpose: prevent reintroduction of `spring.sql.init` as a hidden runtime entry point.
  - Execution: never part of application startup.

## Execution Order By Scenario

### Empty or new database

1. Provision MySQL server, database, account, and privileges.
2. If needed for local/manual bootstrap, run `../init-database.sql` once before application startup.
3. Start the application with Flyway enabled.
4. Flyway applies `B1__baseline_schema.sql -> V2__legacy_schema_alignment.sql -> R__seed_reference_data.sql`.

### Existing non-empty database adopting Flyway for the first time

1. Take a backup or create a clone database first.
2. Ensure the schema already exists and the application account has required DDL/DML permissions.
3. Start the application with `baseline-on-migrate=true` and `baseline-version=1`.
4. Flyway writes `BASELINE(1)`, skips `V1__baseline_schema.sql`, then applies `V2__legacy_schema_alignment.sql` and repeatable migrations.

### Existing database that already contains Flyway version `1`

1. Keep `V1__baseline_schema.sql` unchanged.
2. Start the application normally.
3. Flyway validates the existing history, applies pending versioned migrations such as `V2__legacy_schema_alignment.sql`, then reruns repeatable migrations if checksums changed.

## Verification Commands

### Single clone-database verification

Use the repository-level script below when you only need to validate the non-empty database first-adoption path:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/verify-flyway-clone.ps1 `
  -SourceDatabase cvs `
  -CloneDatabase cvs_flyway_verify
```

The script will:

- package the latest backend jar unless `-SkipPackage` is passed
- recreate the clone database
- import data from the source database with `mysqldump | mysql`
- start the application against the clone database
- print `flyway_schema_history`, startup marker count, and warning counts for `deprecated` / `already exists`

### Three-scenario verification matrix

Use the script below when you need the complete regression matrix for the current migration strategy:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/verify-flyway-matrix.ps1 `
  -SourceDatabase cvs `
  -DatabasePrefix cvs_flyway_matrix
```

The matrix script will verify three scenarios in one run:

- empty/new database: `B1 -> V2 -> R`
- existing non-empty database first adoption: `BASELINE(1) -> V2 -> R`
- existing database with `V1` history: `validate -> pending V* -> pending R*`

The matrix output includes:

- scenario-level startup logs
- `flyway_schema_history` rows for each scenario
- warning counts for `deprecated` / `already exists`
- `matrix-summary.json` for later review and document traceability

### Guardrail validation

Use the script below before merging future migration changes:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/check-flyway-guardrails.ps1
```

The guardrail script checks:

- `spring.sql.init` stays disabled and Flyway baseline strategy stays unchanged
- `V1__baseline_schema.sql` remains frozen at the approved checksum
- `db/migration/*.sql` keeps the approved B/V/R naming boundary
- versioned migrations do not drift into DBA/bootstrap statements
- repeatable migrations do not drift into DDL or DBA/bootstrap statements
- `schema.sql` and `data.sql` remain deprecated placeholders

## Environment Expectations

- Local development
  - `init-database.sql` can be used as a convenience bootstrap if the database/account does not exist.
  - After any migration adjustment, use the clone script or the matrix script before treating the change as accepted.
- Test / acceptance
  - Prefer clone-database or matrix verification instead of pointing a first-run migration at a shared environment.
  - Keep `flyway_schema_history` review and startup log review in the acceptance checklist.
- Production / shared staging
  - Database server, database creation, account creation, credential rotation, and backup are DBA / platform responsibilities.
  - Application startup only owns schema/data migration inside an already provisioned database.
  - First-time Flyway adoption on an existing environment must be preceded by backup plus clone rehearsal.

## Rollback And Change Rules

- Do not edit already-applied versioned migrations to change landed semantics. Add a new `V*.sql` migration instead.
- `V1__baseline_schema.sql` is frozen for compatibility and must stay unchanged.
- `R__seed_reference_data.sql` may evolve, but it must remain idempotent and safe on repeated execution.
- Do not attempt rollback by rerunning `schema.sql`, `data.sql`, or manually invoking old migration files out of order.
- For runtime migration failures or bad schema/data changes, use one of two paths only:
  - restore from backup if the environment must return to a known snapshot
  - deliver a forward corrective migration if the environment should continue on the Flyway chain
- If `init-database.sql` created the wrong database/account, rollback is a manual DBA operation outside the application migration chain.

## Common Parameters

- `-SourceDatabase`: source database to clone, default `cvs`
- `-CloneDatabase`: target clone database for `verify-flyway-clone.ps1`, default `cvs_flyway_verify`
- `-DatabasePrefix`: database prefix for `verify-flyway-matrix.ps1`, default `cvs_flyway_matrix`
- `-MySqlHost` / `-MySqlPort` / `-MySqlUser` / `-MySqlPassword`: MySQL connection overrides
- `-JarPath`: explicit backend jar path if you do not want auto-detection from `backend/target`
- `-LogDirectory`: explicit output directory for startup logs
- `-SkipPackage`: skip backend packaging when a current jar already exists


