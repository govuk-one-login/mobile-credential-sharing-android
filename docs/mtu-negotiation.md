# MTU Negotiation and Data Chunking

## Overview

The Maximum Transmission Unit (MTU) defines the largest payload that can be sent in a single
Bluetooth Low Energy (BLE) packet. The SDK negotiates the highest possible MTU and then 
chunks outgoing messages accordingly.

## MTU Negotiation Flow

### Central (Verifier) Side

After discovering the peripheral's GATT services, the central requests the maximum MTU
(`517` bytes) via `BluetoothGatt.requestMtu()`. The Android Bluetooth stack negotiates with the
peripheral and the result is delivered through the `onMtuChanged` callback.

> On Android 14+ the system always requests 517 regardless of the value passed to `requestMtu()`.
> The explicit call is still made for backwards compatibility with older Android versions.

### Peripheral (Holder) Side

The peripheral receives the negotiated MTU through the `onMtuChanged` callback
(`GattServerCallbackEvent.MtuChanged`) and stores the value. It defaults to the minimum MTU
(`23` bytes) until negotiation completes or if there is no MTU request.

## `MtuValues` Reference

The [MtuValues object] centralises all MTU-related
constants and calculations.

### Constants

| Constant | Value | Description                                                                                     |
|---|---|-------------------------------------------------------------------------------------------------|
| `MAX_MTU` | `517` | Maximum MTU size. Android 14+ always uses this value.                                           |
| `MIN_MTU` | `23` | Minimum / default MTU size.                                                                     |
| `HEADERS` | `3` | ATT protocol header overhead (bytes) subtracted from every packet.                              |
| `ISO_HEADER` | `1` | ISO 18013-5 header byte prepended to each chunk (`0x00` = last chunk, `0x01` = more to follow). |
| `MAX_LENGTH` | `512` | Maximum attribute value length defined in the Bluetooth Core Specification.                     |

### Functions

#### `maxChunkBytes(mtu: Int): Int`

Returns the maximum number of bytes (including the ISO header) that fit in a single BLE
notification for the given MTU.

```
maxChunkBytes = min(mtu − HEADERS, MAX_LENGTH)
```

#### `dataChunkSize(mtu: Int): Int`

Returns the maximum number of **data** bytes per chunk after removing both the ATT headers and the
ISO header.

```
dataChunkSize = maxChunkBytes(mtu) − ISO_HEADER
```

### Examples

| Negotiated MTU | Max Chunk Bytes | Data Chunk Size | Calculation |
|---|------------------|-----------------|---|
| 23 (minimum) | 20               | 19              | min(23 − 3, 512) = 20, then 20 − 1 = 19 |
| 185 | 182              | 181             | min(185 − 3, 512) = 182, then 182 − 1 = 181 |
| 517 (maximum) | 512              | 511             | min(517 − 3, 512) = 512, then 512 − 1 = 511 |

## Data Chunking

When sending a message the peripheral splits the payload into chunks of `dataChunkSize(mtu)` bytes.
Each chunk is prefixed with a 1-byte ISO header:

- `0x01` — more chunks follow
- `0x00` — this is the last (or only) chunk


[MtuValues object]: ../bluetooth/src/main/kotlin/uk/gov/onelogin/sharing/bluetooth/internal/core/MtuValues.kt
