# SDK Lifecycle Redesign: Session-Owned Boundary

## Problem

The SDK's internal DI annotations misrepresent lifetimes and the public API exposes implementation details:

- **Misleading scope annotations** — The orchestrator, BLE transport, crypto services, and 47 other classes are annotated `@SingleIn(AppScope)` / `@ContributesBinding(AppScope)`, implying they're application singletons. In reality, a new graph is created per `createSession()` call, so these are session-scoped objects with app-scoped labels.
- **Leaky public API** — `CredentialPresenter` / `CredentialVerifier` exposes `appGraph` and `orchestrator` on the public surface. Consumers can depend on these internals.
- **No headless-first API** — Headless consumers must reach through `.orchestrator` to access state and actions — an implementation detail that could change.

## Design: Explicit Session Lifecycle

Separate SDK initialisation (app-scoped) from session lifecycle (journey-scoped), with the session as the single public contract and correct DI scope annotations.

### Scope Hierarchy

```
SharingSessionScope (CredentialSharingAppGraph, PresentCredentialGraph, VerifyCredentialGraph)
  ├── lifetime: one createSession() call
  └── provides: Orchestrator, BLE transport, crypto services, session timer, etc.

HolderUiScope / VerifierUiScope (HolderUiGraph / VerifierUiGraph)
  ├── lifetime: one composable UI composition
  └── provides: MetroViewModelFactory, inner screen ViewModels
```

All DI bindings use `SharingSessionScope` — a single, honest scope that reflects the actual object lifetime. No more `AppScope` annotations on objects that are recreated per session.

### SDK Initialisation

```kotlin
// Consumer creates once — scoped however they choose (Activity, Application, etc.)
val sdk = CredentialSharingSdk.create(
    applicationContext = context,
    logger = logger,
    permissionChecker = permissionChecker
)
```

The implementation class (`CredentialSharingSdkImpl`) is `internal` to the SDK module — consumers can only obtain an instance through the `create()` factory method. Previously, consumers imported the implementation class directly, coupling them to the SDK's internal package structure.

### Session Creation

```kotlin
// Holder
val session = sdk.presentCredentialSdk.createSession(credentialProvider)

// Verifier
val session = sdk.verifyCredentialSdk.createSession(verifierConfig)
```

The consumer caches the session in a ViewModel to survive configuration changes. This is a single opaque object — no internal SDK details leak.

### CredentialPresenterSession Interface (Holder)

The session is the single public contract for consumers in both full-UI and headless modes. The SDK's own composables (`ShareCredential` / `VerifyCredential`) access internal session properties (e.g., `appGraph`, orchestrator identity) to construct the UI graph — this is SDK-internal code, not consumer-visible surface.

```kotlin
interface CredentialPresenterSession {
    val sessionState: StateFlow<HolderSessionState>

    fun start()
    fun cancel()
    fun reset()
    fun confirmConsent()
    fun denyConsent()
}
```

### CredentialVerifierSession Interface (Verifier)

```kotlin
interface CredentialVerifierSession {
    val sessionState: StateFlow<VerifierSessionState>

    fun start()
    fun cancel()
    fun reset()
    suspend fun processQrCode(qrCode: String?)
}
```

### Consumption Modes

#### Full UI — consumer hands off the session to the SDK's composable

```kotlin
ShareCredential(session = session)
// or
VerifyCredential(session = session)
```

The composable internally creates the UI dependency graph via `remember`, scoped to the session's orchestrator identity. The consumer only needs to cache the session itself.

#### Headless — consumer drives the session directly with their own UI

```kotlin
session.start()
session.sessionState.collect { state ->
    // consumer renders their own UI based on state
}
```

### Internal Architecture

```
Consumer
  │
  ├── sdk.presentCredentialSdk.createSession(credentialProvider)
  │         │
  │         ▼
  │   CredentialPresenterSession (public interface)
  │         │
  │         ├── wraps Orchestrator.Holder (internal)
  │         └── holds CredentialSharingAppGraph reference (internal)
  │
  ├── sdk.verifyCredentialSdk.createSession(verifierConfig)
  │         │
  │         ▼
  │   CredentialVerifierSession (public interface)
  │         │
  │         ├── wraps Orchestrator.Verifier (internal)
  │         └── holds CredentialSharingAppGraph reference (internal)
  │
  ├── Full UI path:
  │     ShareCredential(session)
  │         │
  │         ▼
  │     remember { HolderUiGraph }
  │         ├── provides MetroViewModelFactory
  │         └── scoped to orchestrator identity
  │
  │     VerifyCredential(session)
  │         │
  │         ▼
  │     remember { VerifierUiGraph }
  │         ├── provides MetroViewModelFactory
  │         └── scoped to orchestrator identity
  │
  └── Headless path:
        session.start() / session.sessionState / etc.
```

### Key Changes from Current Design

| Aspect | Before | After |
|--------|--------|-------|
| Public API surface | `CredentialPresenter` exposes `appGraph` + `orchestrator` | `CredentialPresenterSession` exposes only session actions + state |
| Orchestrator visibility | Public | Internal implementation detail |
| `appGraph` visibility | Public | Internal implementation detail |
| Headless support | Consumer accesses orchestrator directly | Consumer uses `CredentialPresenterSession` / `CredentialVerifierSession` interface |
| Consumer caching | Consumer caches `CredentialPresenter` + understands internals | Consumer caches a single opaque `CredentialPresenterSession` / `CredentialVerifierSession` |
| DI scope annotations | `@SingleIn(AppScope)` on session-lived objects | `@SingleIn(SharingSessionScope)` — annotation matches actual lifetime |

### Migration Path

1. Create `SharingSessionScope` marker and rename `AppScope` → `SharingSessionScope` on all session-lived bindings
2. Create `CredentialPresenterSession` / `CredentialVerifierSession` interfaces wrapping the orchestrator
3. Add `createSession()` methods to `PresentCredentialSdk` / `VerifyCredentialSdk`
4. Update `ShareCredential` / `VerifyCredential` to remove the deprecated `CredentialPresenter` / `CredentialVerifier` overloads
5. Deprecate `presenter()` / `verifier()` methods and the `CredentialPresenter` / `CredentialVerifier` interfaces
6. Remove `appGraph` and `orchestrator` from the public API surface

### Benefits

- **Honest DI annotations** — `SharingSessionScope` reflects the true lifetime of session objects; developers reading the code understand what lives where
- **Cleaner public API** — consumers see only what they need: session state and actions
- **Both consumption modes use the same object** — full UI and headless share `CredentialPresenterSession` / `CredentialVerifierSession`
- **Reduced integration surface** — consumer caches one opaque object instead of understanding `appGraph` + `orchestrator` + `CredentialPresenter` wiring
- **SDK controls its own invariants** — internal DI graph, orchestrator creation, and crypto context are fully encapsulated
- **Safer evolution** — SDK internals can change without breaking consumers since only the session interface is public

### Consumer Migration Guide

#### Holder (credential sharing)

**Before:**

```kotlin
// Creating
val presenter = sdk.presentCredentialSdk.presenter(credentialProvider)

// Caching in a ViewModel
class MyViewModel : ViewModel() {
    val presenter: CredentialPresenter = ...
}

// Full UI
ShareCredential(component = presenter)

// Headless — consumer must reach into orchestrator (an internal detail)
presenter.orchestrator.holderSessionState.collect { ... }
presenter.orchestrator.start()
presenter.orchestrator.confirmConsent()
```

**After:**

```kotlin
// Creating
val session = sdk.presentCredentialSdk.createSession(credentialProvider)

// Caching in a ViewModel
class MyViewModel : ViewModel() {
    val session: CredentialPresenterSession = ...
}

// Full UI
ShareCredential(session = session)

// Headless — consumer uses the session directly
session.sessionState.collect { ... }
session.start()
session.confirmConsent()
```

#### Verifier (credential verification)

**Before:**

```kotlin
// Creating
val verifier = sdk.verifyCredentialSdk.verifier(verifierConfig)

// Caching in a ViewModel
class MyViewModel : ViewModel() {
    val verifier: CredentialVerifier = ...
}

// Full UI
VerifyCredential(component = verifier)

// Headless — consumer must reach into orchestrator (an internal detail)
verifier.orchestrator.verifierSessionState.collect { ... }
verifier.orchestrator.start()
verifier.orchestrator.processQrCode(qrCode)
```

**After:**

```kotlin
// Creating
val session = sdk.verifyCredentialSdk.createSession(verifierConfig)

// Caching in a ViewModel
class MyViewModel : ViewModel() {
    val session: CredentialVerifierSession = ...
}

// Full UI
VerifyCredential(session = session)

// Headless — consumer uses the session directly
session.sessionState.collect { ... }
session.start()
session.processQrCode(qrCode)
```

#### Notes

- `createSession()` creates the orchestrator internally — call it once and cache the result.
- The old `presenter()` / `verifier()` methods and `CredentialPresenter` / `CredentialVerifier` interfaces are deprecated and will be removed in a future release.
- `appGraph` on `CredentialSharingSdk` is deprecated — consumers should not access `CredentialSharingAppGraph` directly.

#### Naming Alternatives

The chosen names (`CredentialPresenterSession` / `CredentialVerifierSession`) maintain familiarity with the existing `CredentialPresenter` / `CredentialVerifier` terminology while the `Session` suffix signals the new lifecycle-aware API. Alternatives considered:

- `PresenterSession` / `VerifierSession` — shorter, less repetition of "Credential"
- `SharingSession` / `VerificationSession` — more descriptive of the action, cleaner break from old names
- `HolderSession` / `VerifierSession` — aligns with ISO 18013-5 role terminology
