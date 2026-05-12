#!/usr/bin/env python3
"""
Generates a mock_credential.txt for the Sharing SDK test app.

Requirements:
    pip install cbor2 cryptography

Usage:
    python generate_mock_credential.py --private-key app/src/main/assets/test_private_key.pem \
                                       --output app/src/main/res/raw/mock_credential.txt

The generated credential uses the device key from the provided PEM file and creates
a self-signed issuer certificate. The credential is valid for 1 year.
"""

import argparse
import base64
import cbor2
import hashlib
from datetime import datetime, timezone, timedelta
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric.utils import decode_dss_signature
from cryptography.x509 import CertificateBuilder, Name, NameAttribute
from cryptography.x509.oid import NameOID
from cryptography import x509

# Sample IssuerSignedItems for a test mDL
SAMPLE_ITEMS = {
    "org.iso.18013.5.1": [
        {"digestID": 1, "elementIdentifier": "family_name", "elementValue": "Doe"},
        {"digestID": 2, "elementIdentifier": "given_name", "elementValue": "Jane"},
        {"digestID": 3, "elementIdentifier": "birth_date", "elementValue": cbor2.CBORTag(1004, "1990-01-15")},
        {"digestID": 4, "elementIdentifier": "issue_date", "elementValue": cbor2.CBORTag(1004, "2024-01-01")},
        {"digestID": 5, "elementIdentifier": "expiry_date", "elementValue": cbor2.CBORTag(1004, "2034-01-01")},
        {"digestID": 6, "elementIdentifier": "issuing_country", "elementValue": "GB"},
        {"digestID": 7, "elementIdentifier": "issuing_authority", "elementValue": "DVLA"},
        {"digestID": 8, "elementIdentifier": "age_over_18", "elementValue": True},
        {"digestID": 9, "elementIdentifier": "age_over_21", "elementValue": True},
        {"digestID": 10, "elementIdentifier": "age_over_25", "elementValue": True},
    ],
    "org.iso.18013.5.1.GB": [
        {"digestID": 11, "elementIdentifier": "welsh_licence", "elementValue": False},
    ],
}


def build_issuer_signed_item(item):
    """Encode an IssuerSignedItem as Tag(24, bstr(encoded_item))."""
    item_with_random = {
        "digestID": item["digestID"],
        "random": hashlib.sha256(str(item["digestID"]).encode()).digest()[:16],
        "elementIdentifier": item["elementIdentifier"],
        "elementValue": item["elementValue"],
    }
    encoded = cbor2.dumps(item_with_random)
    return cbor2.CBORTag(24, encoded)


def main():
    parser = argparse.ArgumentParser(description="Generate a mock credential for the test app")
    parser.add_argument("--private-key", required=True, help="Path to device private key PEM")
    parser.add_argument("--output", required=True, help="Output path for credential txt file")
    parser.add_argument("--validity-days", type=int, default=365, help="Validity period in days")
    args = parser.parse_args()

    # Load device private key
    with open(args.private_key, "rb") as f:
        device_private_key = serialization.load_pem_private_key(f.read(), password=None)

    device_pub = device_private_key.public_key()
    device_nums = device_pub.public_numbers()
    device_x = device_nums.x.to_bytes(32, "big")
    device_y = device_nums.y.to_bytes(32, "big")

    # Build nameSpaces
    namespaces = {}
    for ns_name, items in SAMPLE_ITEMS.items():
        namespaces[ns_name] = [build_issuer_signed_item(item) for item in items]

    # Generate issuer key and self-signed certificate
    issuer_private_key = ec.generate_private_key(ec.SECP256R1())
    issuer_pub = issuer_private_key.public_key()
    now = datetime.now(timezone.utc)
    cert = (
        CertificateBuilder()
        .subject_name(Name([
            NameAttribute(NameOID.COMMON_NAME, "mDoc Test Issuer"),
            NameAttribute(NameOID.ORGANIZATION_NAME, "DVLA Dev Tool"),
        ]))
        .issuer_name(Name([
            NameAttribute(NameOID.COMMON_NAME, "mDoc Test Issuer"),
            NameAttribute(NameOID.ORGANIZATION_NAME, "DVLA Dev Tool"),
        ]))
        .public_key(issuer_pub)
        .serial_number(x509.random_serial_number())
        .not_valid_before(now)
        .not_valid_after(now + timedelta(days=args.validity_days))
        .add_extension(x509.SubjectKeyIdentifier.from_public_key(issuer_pub), critical=False)
        .add_extension(x509.BasicConstraints(ca=True, path_length=None), critical=True)
        .sign(issuer_private_key, hashes.SHA256())
    )
    cert_der = cert.public_bytes(serialization.Encoding.DER)

    # Build MSO
    device_cose_key = {1: 2, -1: 1, -2: device_x, -3: device_y}

    value_digests = {}
    for ns_name, items in namespaces.items():
        ns_digests = {}
        for item in items:
            item_bytes = cbor2.dumps(item)
            decoded_item = cbor2.loads(item.value)
            ns_digests[decoded_item["digestID"]] = hashlib.sha256(item_bytes).digest()
        value_digests[ns_name] = ns_digests

    mso = {
        "version": "1.0",
        "digestAlgorithm": "SHA-256",
        "valueDigests": value_digests,
        "deviceKeyInfo": {
            "deviceKey": device_cose_key,
            "keyAuthorizations": {"nameSpaces": list(namespaces.keys())},
        },
        "docType": "org.iso.18013.5.1.mDL",
        "validityInfo": {
            "signed": cbor2.CBORTag(0, now.strftime("%Y-%m-%dT%H:%M:%SZ")),
            "validFrom": cbor2.CBORTag(0, now.strftime("%Y-%m-%dT%H:%M:%SZ")),
            "validUntil": cbor2.CBORTag(0, (now + timedelta(days=args.validity_days)).strftime("%Y-%m-%dT%H:%M:%SZ")),
        },
    }

    mso_bytes = cbor2.dumps(mso)
    mso_tagged = cbor2.dumps(cbor2.CBORTag(24, mso_bytes))

    # Sign (COSE_Sign1)
    protected_header = cbor2.dumps({1: -7})
    sig_structure = cbor2.dumps(["Signature1", protected_header, b"", mso_tagged])
    signature_der = issuer_private_key.sign(sig_structure, ec.ECDSA(hashes.SHA256()))
    r, s = decode_dss_signature(signature_der)
    signature = r.to_bytes(32, "big") + s.to_bytes(32, "big")

    issuer_auth = [protected_header, {33: cert_der}, mso_tagged, signature]

    # Assemble credential (flat format)
    credential = {"nameSpaces": namespaces, "issuerAuth": issuer_auth}
    credential_bytes = cbor2.dumps(credential)
    credential_b64 = base64.urlsafe_b64encode(credential_bytes).rstrip(b"=").decode()

    with open(args.output, "w") as f:
        f.write(credential_b64)

    print(f"Generated: {args.output}")
    print(f"  Device key: {device_x.hex()[:16]}...")
    print(f"  Valid until: {(now + timedelta(days=args.validity_days)).isoformat()}")
    print(f"  Namespaces: {list(namespaces.keys())}")
    print(f"  Size: {len(credential_bytes)} bytes")


if __name__ == "__main__":
    main()
