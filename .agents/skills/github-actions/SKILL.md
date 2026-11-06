---
name: github-actions
description: Best practices for authoring and maintaining GitHub Actions workflows
  in this project. Covers pinned action versions, reusable workflows, secrets handling,
  concurrency, and Android-specific CI patterns.
metadata:
  author: GOV.UK One Login
  last-updated: '2025-07-22'
  keywords:
  - github-actions
  - ci
  - cd
  - workflows
  - android
---

## Step 1: Understand the existing workflow structure

Before making any changes, read the existing workflows in `.github/workflows/`:

- `pull-request.yml` — triggered on PRs; calls `linting.yml` and `testing.yml` as reusable workflows
- `linting.yml` — runs conventional commits, prose (Vale), Android lint, and Kotlin lint
- `testing.yml` — runs contract, unit, and instrumentation tests, then a SonarCloud scan
- `merge-to-main.yml` — runs on merge to main

Also check `.github/actions/android-setup/action.yml` for the shared setup step used across jobs.

## Step 2: Pinned action versions

All third-party GitHub Actions **must** be pinned to a full commit SHA, not a tag. This project already enforces this pattern:

```yaml
# Correct
uses: actions/checkout@9c091bb21b7c1c1d1991bb908d89e4e9dddfe3e0 # v7.0.0

# Wrong - never use floating tags
uses: actions/checkout@v4
uses: actions/checkout@main
```

- Always include the human-readable version as a comment after the SHA
- When updating an action, update both the SHA and the comment
- First-party actions (`./.github/actions/...` or `./mobile-android-pipelines/actions/...`) are exempt — use path references

## Step 3: Reusable workflows

This project uses reusable workflows (`workflow_call`) to avoid duplication. Follow this pattern when adding new jobs:

- If a job is needed in more than one workflow, extract it into its own reusable workflow file
- Reusable workflows must declare `workflow_call` as a trigger
- Pass secrets with `secrets: inherit` from the calling workflow
- Keep the calling workflow (e.g. `pull-request.yml`) thin — it should only orchestrate calls to reusable workflows

## Step 4: Concurrency

Every workflow must define a `concurrency` block to cancel redundant runs:

```yaml
concurrency:
  group: <prefix>-${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true
```

Use a meaningful prefix (e.g. `pr`, `testing`, `linting`) to avoid collisions between workflows running on the same ref.

## Step 5: Secrets and environment variables

- Never hardcode tokens or credentials in workflow files
- Use `${{ secrets.GITHUB_TOKEN }}` for package registry access
- Use `${{ secrets.SONAR_TOKEN }}` for SonarCloud
- Declare secrets as top-level `env` variables so they are available to all steps:

```yaml
env:
  GITHUB_ACTOR: ${{ github.actor }}
  GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

- Set the minimum required `permissions` at the workflow level — this project uses `packages: read` for most workflows

## Step 6: Android-specific CI patterns

- Always use the shared setup action (`./.github/actions/android-setup`) for environment setup
- Pass `install-vale: 'false'` when the job does not run prose linting, to keep setup fast
- Instrumentation tests use `./mobile-android-pipelines/actions/setup-runner` instead of `android-setup` — check existing jobs before changing the setup action
- Use `--no-configuration-cache` for instrumentation test runs (known incompatibility)
- Always upload test coverage reports as artifacts with `if: always()` so they are available even on failure

## Step 7: Coverage and SonarCloud

- The `sonar_scan` job depends on all three test jobs (`needs: [contract-debug-tests, unit_tests, instrumentation_tests]`)
- Coverage artifacts must be downloaded before the Sonar scan step
- Do not remove or reorder the `needs` dependencies on the Sonar job

## Step 8: Dependabot

- `dependabot.yml` manages automated dependency updates — do not manually update action SHAs that Dependabot manages
- Check `dependabot.yml` before pinning a new action to ensure it will be kept up to date automatically
