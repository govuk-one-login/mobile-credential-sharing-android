# COSE Verification Independent Test Vectors

This directory contains test material for COSE_Sign1 verification, generated independently from the production Android implementation.

## Provenance
Vectors were generated using a custom Python script using the `cryptography` and `cbor2` libraries to ensure independence from the project's Jackson-based encoding logic.

## Vectors

| File | Mode | Description | SHA-256 Hash |
| :--- | :--- | :--- | :--- |
| `attached_mso.hex` | Attached | Valid ES256 structure with inline payload (MSO) | 6579e37199f4e15677f6c9af45a9eea7724be92fead7de782084f4fc50964bf6 |
| `detached_reader.hex` | Detached | Valid ES256 structure with null payload (Reader Auth) | 80833a0f5541ed000f6c712c09b20bb3f5145934a8a212fca09b49243e872e75 |
| `detached_device.hex` | Detached | Valid ES256 structure with null payload (Device Auth) | 172e2e46e1f599770ab49e8f38a967574b8568876a345d512569d2eed6de651d |
