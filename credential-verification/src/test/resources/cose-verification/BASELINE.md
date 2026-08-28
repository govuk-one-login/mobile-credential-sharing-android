# Verification Baseline

## Recorded Outcomes (as of C1A)

| Scenario | Current Behaviour (Trust) | Target Behaviour (COSE) |
| :--- | :--- | :--- |
| Attached IssuerAuth | Passes (Jackson readTree) | Strict 4-element check, no Tag 18 |
| Direct-key DeviceSignature | Passes | Strict 4-element check |
| Malformed Input | Throws generic Failure | Throws MalformedCoseSign1 |
| Invalid Signature | Throws generic Failure | Throws InvalidSignature |
| Wrong Root | Throws generic Failure | Throws UntrustedCertificate |
| Protected x5chain Fallback | Supported (Legacy) | **REMOVED** (Header must be in unprotected) |
| Shuffled Chain | Repaired & Verified | **REMOVED** (Must preserve order) |
