# Project Rules

## Overview

This is an ISO 18013-5 compliant Android SDK for mobile credential sharing. It implements two roles:

- **Holder** (`holder/`) — shares credentials with a verifier
- **Verifier** (`verifier/`) — requests and validates credentials from a holder

## Module Structure

| Module                    | Purpose                               |
|---------------------------|---------------------------------------|
| `app`                     | Demo application                      |
| `bluetooth`               | BLE transport layer                   |
| `camera-service`          | QR code scanning                      |
| `core`                    | Shared utilities                      |
| `credential-format`       | CBOR credential models                |
| `credential-verification` | Signature and integrity validation    |
| `crypto-service`          | mdoc session encryption/decryption    |
| `exchange-format`         | DeviceRequest / DeviceResponse models |
| `holder`                  | Credential presentation session       |
| `orchestration`           | Session lifecycle coordination        |
| `prerequisite-gate-api`   | Transport availability API            |
| `prerequisite-gate-impl`  | Transport availability implementation |
| `sdk`                     | Public SDK entry point                |
| `ui:ui-api`               | UI contracts                          |
| `ui:ui-impl`              | UI implementations                    |
| `verifier`                | Credential verification session       |

## Language & Tooling

- Kotlin 2.4.0, AGP 9.2.1
- Jetpack Compose with Material3
- Hilt (DI) and Metro
- Detekt + ktlint (android_studio code style) for static analysis
- Robolectric + MockK + Turbine for unit tests
- Roborazzi for screenshot tests

## Dependency Management

All dependency versions are managed centrally in `gradle/libs.versions.toml`. Always reference this
file when adding or updating dependencies — never hardcode versions in `build.gradle.kts` files.

## Coding Conventions

- Follow existing Detekt rules (`detekt.yml`) — `warningsAsErrors` is off but violations should be
  avoided
- Composable functions use TitleCase naming and may have long parameter lists — both are permitted
- Guard clauses are excluded from `ReturnCount` checks
- Preview functions follow the `.*?Preview` naming pattern
- CBOR byte arrays from issuers must be preserved exactly — never re-encode issuer-signed data
- Bouncycastle must not be used in production `api` or `implementation` dependencies — test only

## Skills

Before starting any task, read the relevant skill from `.agents/skills/`:

| Task                                | Skill                                  |
|-------------------------------------|----------------------------------------|
| AGP / build changes                 | `agp-9-upgrade`                        |
| Adaptive / responsive UI            | `adaptive`                             |
| Adding new AI agent skills          | `find-skills`                          |
| AppFunctions                        | `appfunctions`                         |
| Camera / QR scanning work           | `camerax`                              |
| Compose Styles API                  | `styles`                               |
| Edge-to-edge UI                     | `edge-to-edge`                         |
| GitHub Actions / CI workflows       | `github-actions`                       |
| Intent handling                     | `android-intent-security`              |
| Migrating XML Views to Compose      | `migrate-xml-views-to-jetpack-compose` |
| Navigation changes                  | `navigation-3`                         |
| Performance / trace analysis        | `perfetto-trace-analysis`              |
| R8 / ProGuard rules                 | `r8-analyzer`                          |
| Verified email / Credential Manager | `verified-email`                       |
| Writing or updating tests           | `testing-setup`                        |

At the start of every conversation, use `skills_list` to check which skills are installed.
If any skills listed in the Skills table above are missing, ask the user if they want to
install them by running:

```bash
./scripts/ai/android/install-skills
```
