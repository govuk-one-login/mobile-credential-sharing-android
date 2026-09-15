# Workflow Architecture

## Trigger Map

| Workflow | Triggers |
|---|---|
| `pull-request.yml` | `pull_request` (opened, reopened, synchronize), `workflow_dispatch` |
| `linting.yml` | `push` to main, `merge_group`, `workflow_call`, `workflow_dispatch` |
| `testing.yml` | `push` to main, `merge_group`, `workflow_call`, `workflow_dispatch` |
| `merge-to-main.yml` | `push` to main |

## Job Map

### linting.yml
- `conventional_commits` — runs `cog check -l` against latest git tag
- `prose_linting` — runs Vale with styles from `.github/styles/`
- `android_linting` — runs `./scripts/lint/android`
- `kotlin_linting` — runs `./scripts/lint/kotlin`

### testing.yml
- `contract-debug-tests` — runs `./scripts/test/contract/debug`
- `unit_tests` — runs `./scripts/test/unit/debug`
- `instrumentation_tests` — runs `./scripts/test/instrumentation/debug --no-configuration-cache`
- `sonar_scan` — depends on all three test jobs; downloads coverage artifacts and runs SonarCloud scan

## Shared Setup Actions

| Action | Used by | Notes |
|---|---|---|
| `./.github/actions/android-setup` | Most jobs | Pass `install-vale: 'false'` unless running prose linting |
| `./mobile-android-pipelines/actions/setup-runner` | `instrumentation_tests` | Configures Gradle cache and JDK 21 |

## Coverage Artifact Names

| Job | Artifact name |
|---|---|
| `contract-debug-tests` | `contract-debug-test-coverage` |
| `unit_tests` | `unit-test-coverage` |
| `instrumentation_tests` | `instrumentation-test-coverage` |
