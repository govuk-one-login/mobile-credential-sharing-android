---
name: cbor-decoding
description: Conventions for CBOR decoding, MSO mapping, and Jackson ObjectMapper configuration in the credential-verification module. Use when writing or reviewing decoders, DTOs, or CBOR-related code.
---
# CBOR Decoding & Mapping

Guidelines for decoding CBOR structures (ISO 18013-5 MSO, COSE headers, device auth) in this project.

## ObjectMapper Configuration

Use `JsonMapper.builder(CBORFactory())` with `StreamReadFeature.STRICT_DUPLICATE_DETECTION` enabled for any decoder that parses untrusted CBOR maps. This replaces manual duplicate-key scanning.

```kotlin
private val cborMapper = JsonMapper.builder(CBORFactory())
    .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
    .build()
```

Reference implementations:
- `MsoDecoder` — credential-verification module
- `CoseHeaderValidator` — credential-verification module

For classes that only serialise/write CBOR (e.g. `CoseSignatureVerifier`, `DeviceAuthenticationEncoder`), a plain `ObjectMapper(CBORFactory())` is acceptable since duplicate detection only matters on read.

## DTO Pattern

Separate the Jackson-annotated DTO from the domain model:

1. **DTO class** (`internal data class FooDto`) — handles Jackson deserialisation, annotated with `@JsonDeserialize(using = ...)`.
2. **Domain model** (`data class Foo`) — validated, immutable, no Jackson annotations. Validation logic lives in `init {}`.
3. **Mapping** — DTO exposes `fun toDomain(): Foo` that converts and triggers domain validation.

```kotlin
@JsonDeserialize(using = MsoDto.MsoDtoDeserializer::class)
internal data class MsoDto(...) {
    fun toDomain(): MobileSecurityObject = MobileSecurityObject(...)
}
```

### Custom Deserialiser

Use a `StdDeserializer` inside the DTO companion/nested class. Fail fast with `VerificationResult.Failure` for:
- Missing required fields
- Wrong types (e.g. non-integer digest IDs)
- Invalid timestamp formats

Timestamps must match `\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z` (UTC, no fractional seconds, no numeric offsets).

## Decoder Pattern

A decoder class:
- Is annotated with `@Inject` for Metro DI.
- Has a single public `fun decode(bytes: ByteArray): DomainModel` method.
- Catches `VerificationResult.Failure` and rethrows it; catches all other exceptions and wraps them in the appropriate `VerificationError`.

```kotlin
@Inject
class MsoDecoder {
    fun decode(encodedMso: ByteArray): MobileSecurityObject = try {
        // unwrap, deserialise, map to domain
    } catch (e: VerificationResult.Failure) {
        throw e
    } catch (_: Exception) {
        throw VerificationResult.Failure(VerificationError.MALFORMED_MSO)
    }
}
```

## Tag24 Unwrapping

ISO 18013-5 payloads are wrapped in CBOR Tag 24 (embedded CBOR). Unwrap by reading the outer value as a `BinaryNode`:

```kotlin
private fun unwrapTag24(data: ByteArray): ByteArray {
    val root = cborMapper.readTree(data)
    return (root as? BinaryNode)?.binaryValue() ?: throw malformed
}
```

## Test Stubs

Reusable CBOR test data lives in `MobileSecurityObjectStubs` (credential-format `testFixtures`):
- Use the exposed `buildMsoBytes(...)` to construct custom MSO byte arrays.
- Use `wrapTag24(...)` to wrap raw bytes in Tag 24.
- Use pre-built vals (e.g. `validEncodedMSO`, `encodedMsoWithDuplicateKeys`) for common scenarios.
- Keep helper functions that are only used to build pre-built vals `private`.

## Testing

- **Decoder tests** go in the decoder's own test class (e.g. `MsoDecoderTest`). Cover: valid decode, missing fields, invalid types, bad timestamps, duplicate keys, tag24 unwrapping, domain validation errors.
- **Consumer tests** (e.g. `Iso18013DocumentVerifierTest`) only test that the consumer integrates correctly with the decoder (e.g. malformed MSO propagates `MALFORMED_MSO` through `verifyDocument`). Do not duplicate decoder-level tests.
- **DTO tests** (e.g. `MsoDtoTest`) test Jackson deserialisation and `toDomain()` mapping independently.
