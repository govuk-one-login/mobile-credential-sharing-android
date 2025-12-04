# Mobile | Credential sharing | Android

A reference implementation for sharing and verifying digital credentials.

The current implementation includes a demo app and implements ISO 18013-5 for in-person Bluetooth presentation and verification.

Internal team members can find our ways of working on Confluence.

## Overview

This repository contains packages for: 

- [Models](./models): representing data models in CBOR format
- [Bluetooth](./bluetooth): sharing data over Bluetooth
- [Security](./security): encryption and decryption of data for transit
- [Holder](./holder): securely share a credential with a verifier
- [Verifier](./verifier): securely receive and verify a credential from a holder


```mermaid
classDiagram
namespace Holder {
    class CredentialPresentationSession
}

namespace Verifier {
    class CredentialVerificationSession
}

namespace Models {
    class DeviceEngagement
    class SessionEstablishment
    class DeviceRequest
    class DeviceResponse
}

namespace Security {
    class EncryptionSession
    class DecryptionSession
}

namespace BluetoothTransmission {
    class BluetoothCommunicationSession{
        <<interface>>
        sendMessage(Data data)
    }
    class BluetoothCentralSession
    class BluetoothPeripheralSession
}

BluetoothCommunicationSession<|--BluetoothCentralSession
BluetoothCommunicationSession <|-- BluetoothPeripheralSession
```

More details coming soon.

## Setup and installation

- The [Documentation] relating to project configuration and developer set up.

[Documentation]: /docs

## Usage

### Using the test app

TBC

### Consuming the SDK

#### For presenting

TBC

#### For verifying

We recommend that you start by reading the GOV.UK Wallet [Technical Documentation](https://docs.wallet.service.gov.uk/consuming-and-verifying-credentials)

TBC
