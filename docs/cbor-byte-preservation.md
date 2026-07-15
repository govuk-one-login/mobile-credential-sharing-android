# CBOR Byte Preservation

## Overview

ISO 18013-5 section 8.3 requires that CBOR structures with a "Bytes" suffix (e.g.,
`IssuerSignedItemBytes`, `DeviceAuthenticationBytes`) are preserved exactly as received for cryptographic operations. The SDK must never decode and re-encode these
structures.

> For any cryptographic operation, an mdoc, mdoc reader or issuing authority infrastructure shall
> use these bytestrings as they were sent or received, without attempting to re-create them from
> the underlying maps.
>
> — ISO 18013-5 §8.3

## Why re-encoding breaks verification

CBOR has multiple valid encodings for the same logical value. Signatures and digests are computed
over a specific byte sequence. If the bytes are re-encoded, the resulting output may differ from
the original even if the content is identical. This causes signature and digest
verification to fail.

Re-encoding through Jackson introduces several issues:

| Problem | Example | Impact |
|---|---|---|
| Integer map keys become strings | `{1: -7}` → `{"1": -7}` | COSE protected header corruption |
| Map field ordering changes | Fields reordered alphabetically | Hash mismatch |
| Numeric encoding width changes | 1-byte int → 2-byte int | Byte-level difference |
| Tag handling differences | Tag 24 wrapping may not round-trip | Envelope corruption |

## DeviceResponseCborExtractor

The [DeviceResponseCborExtractor] solves this by slicing the original source bytes at exact
parser offsets — never decoding and re-encoding the cryptographically significant structures.

### What it extracts

The extractor returns a `DocumentRawBytes` per document, containing two nested structures:

**`IssuerSignedRawBytes`**

| Field | ISO reference | Purpose |
|---|---|---|
| `nameSpaces` | 9.1.2.4 | Per-namespace list of Tag 24-wrapped IssuerSignedItem bytes for digest verification |
| `issuerAuthBytes` | 9.1.2 | Issuer signature verification (COSE_Sign1) |

**`DeviceSignedRawBytes`**

| Field | ISO reference | Purpose |
|---|---|---|
| `nameSpacesBytes` | 9.1.3 | Tag 24-wrapped DeviceNameSpaces for DeviceAuthentication construction |
| `signatureBytes` | 9.1.3 | Device signature verification (COSE_Sign1) |

### How it works

1. The `DeviceResponse` is deserialized normally via Jackson for structural access (field names,
   document types, consent UI).
2. In parallel, `DeviceResponseCborExtractor` walks the same source bytes with a low-level
   `CBORParser`, recording byte offsets at the start and end of each cryptographic structure.
3. It returns `source.copyOfRange(start, end)` — the original bytes, untouched.
4. During `toDomain()`, these preserved bytes are preferred over any re-encoded values from the
   Jackson tree.


## Serialization (DeviceResponseSerializer)

The same byte preservation requirement applies when the Holder constructs a `DeviceResponse` to
send. The [DeviceResponseSerializer] preserves bytes using two mechanisms:

### RawCbor

Fields like `issuerAuth` and `deviceSignature` are stored as `RawCbor` wrappers. During
serialization, the generator is flushed and the raw bytes are written directly to the output
stream, bypassing Jackson's encoding entirely:

```kotlin
gen.flush()
(gen.outputTarget as OutputStream).write(issuerSigned.issuerAuth.encoded)
```

### EmbeddedCbor

Fields like `IssuerSignedItem` entries and `DeviceNameSpaces` are stored as `EmbeddedCbor`
wrappers. During serialization, they are written as Tag 24-wrapped byte strings — the inner
bytes are never decoded or re-encoded:

```kotlin
gen.writeTag(EMBEDDED_CBOR_TAG)
gen.writeBinary(item.encoded)
```

### Summary

| Direction | Mechanism | Bypass method |
|---|---|---|
| Deserialization (Verifier) | `DeviceResponseCborExtractor` | Offset-based byte slicing |
| Serialization (Holder) | `RawCbor` / `EmbeddedCbor` | Direct output stream write |

## Guidelines

- **Never re-encode** a structure that will be used in a cryptographic operation.
- **Always use** the raw bytes from `DeviceResponseCborExtractor` for signature/digest input
  on the Verifier side.
- **Always use** `RawCbor` or `EmbeddedCbor` wrappers for cryptographic structures on the
  Holder side — never serialize via Jackson's tree model.
- **Do not remove** the extractor even if tests pass without it — tests may use simple values
  that happen to round-trip correctly, but real-world COSE structures will not.
- When adding new cryptographic fields, add corresponding extraction in
  `DeviceResponseCborExtractor` and use `RawCbor`/`EmbeddedCbor` in the DTO/serializer.

[DeviceResponseCborExtractor]: ../exchange-format/src/main/kotlin/uk/gov/onelogin/sharing/models/mdoc/sessionEstablishment/deviceResponse/DeviceResponseCborExtractor.kt
[DeviceResponseSerializer]: ../exchange-format/src/main/kotlin/uk/gov/onelogin/sharing/models/mdoc/sessionEstablishment/deviceResponse/DeviceResponseDto.kt
