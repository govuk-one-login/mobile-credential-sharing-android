#!/usr/bin/env python3
"""
Generates a mock_credential.txt for the Sharing SDK test app.

Requirements:
    pip install cbor2 cryptography

Usage:
    python3 scripts/mock_credential/generate_mock_credential.py \
    --issuer-private-key app/src/main/assets/test_private_issuer_key.pem \
    --private-key app/src/main/assets/test_private_key.pem \
    --x509-certificate app/src/main/assets/test_x509_certificate.der \
    --output app/src/main/res/raw/mock_credential.txt \

The generated credential uses the device key from the provided PEM file and creates
a 2-cert chain: a self-signed root CA and a leaf certificate signed by the root.
The root certificate (.der) is the trust anchor for the verifier app.
The leaf certificate is embedded in the credential's issuerAuth x5chain.
"""

import argparse
import base64
import cbor2
import hashlib
import os
from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives.asymmetric.utils import decode_dss_signature
from cryptography.x509 import Certificate, CertificateBuilder, Name, NameAttribute
from cryptography.x509.oid import NameOID, ExtendedKeyUsageOID, ObjectIdentifier
from datetime import datetime, timezone, timedelta
from typing import Optional
from mock_credential.issuer import IssuerAuthInput

# Sample IssuerSignedItems for a test mDL
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PORTRAIT_BYTES = base64.b64decode(
    open(os.path.join(SCRIPT_DIR, "portrait.txt")).read().strip()
)

# ISO 18013-5 OIDs
OID_MDL_DS = ObjectIdentifier("1.0.18013.5.1.2")

SAMPLE_ITEMS = {
    "org.iso.18013.5.1": [
        {"digestID": 1, "elementIdentifier": "family_name", "elementValue": "Doe"},
        {"digestID": 2, "elementIdentifier": "given_name", "elementValue": "Jane"},
        {"digestID": 3, "elementIdentifier": "portrait", "elementValue": PORTRAIT_BYTES},
        {"digestID": 4, "elementIdentifier": "birth_date",
         "elementValue": cbor2.CBORTag(1004, "2007-01-15")},
        {"digestID": 5, "elementIdentifier": "issue_date",
         "elementValue": cbor2.CBORTag(1004, "2024-01-01")},
        {"digestID": 6, "elementIdentifier": "expiry_date",
         "elementValue": cbor2.CBORTag(1004, "2034-01-01")},
        {"digestID": 7, "elementIdentifier": "issuing_country", "elementValue": "GB"},
        {"digestID": 8, "elementIdentifier": "issuing_authority", "elementValue": "DVLA"},
        {"digestID": 9, "elementIdentifier": "age_over_18", "elementValue": True},
        {"digestID": 10, "elementIdentifier": "age_over_21", "elementValue": False},
        {"digestID": 11, "elementIdentifier": "age_over_25", "elementValue": False},
    ],
    "org.iso.18013.5.1.GB": [
        {"digestID": 12, "elementIdentifier": "welsh_licence", "elementValue": False},
    ],
}

ISSUER_NAME = Name([
    NameAttribute(NameOID.COUNTRY_NAME, "GB"),
    NameAttribute(NameOID.STATE_OR_PROVINCE_NAME, "London"),
    NameAttribute(NameOID.COMMON_NAME, "mDoc Test Issuer"),
    NameAttribute(NameOID.ORGANIZATION_NAME, "DVLA Dev Tool"),
])

LEAF_NAME = Name([
    NameAttribute(NameOID.COUNTRY_NAME, "GB"),
    NameAttribute(NameOID.STATE_OR_PROVINCE_NAME, "London"),
    NameAttribute(NameOID.COMMON_NAME, "mDoc Test Leaf"),
    NameAttribute(NameOID.ORGANIZATION_NAME, "DVLA Dev Tool"),
])


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


def generate_root_certificate(issuer_private_key, now, validity_days):
    """Generate a self-signed root CA certificate."""
    issuer_pub = issuer_private_key.public_key()
    return (
        CertificateBuilder()
        .subject_name(ISSUER_NAME)
        .issuer_name(ISSUER_NAME)
        .public_key(issuer_pub)
        .serial_number(x509.random_serial_number())
        .not_valid_before(now)
        .not_valid_after(now + timedelta(days=validity_days))
        .add_extension(
            x509.SubjectKeyIdentifier.from_public_key(issuer_pub), critical=False
        )
        .add_extension(
            x509.AuthorityKeyIdentifier.from_issuer_public_key(issuer_pub), critical=False
        )
        .add_extension(
            x509.BasicConstraints(ca=True, path_length=None), critical=True
        )
        .add_extension(
            x509.KeyUsage(
                key_cert_sign=True, crl_sign=True,
                digital_signature=False, content_commitment=False,
                key_encipherment=False, data_encipherment=False,
                key_agreement=False, encipher_only=False, decipher_only=False
            ), critical=True
        )
        .add_extension(
            x509.IssuerAlternativeName([
                x509.UniformResourceIdentifier("https://dvla.gov.uk/iaca")
            ]), critical=False
        )
        .sign(issuer_private_key, hashes.SHA256())
    )


def generate_leaf_certificate(leaf_private_key, issuer_private_key, root_cert, now, validity_days):
    """Generate a leaf certificate signed by the root, with IACA-compliant extensions."""
    leaf_pub = leaf_private_key.public_key()
    issuer_pub = issuer_private_key.public_key()
    leaf_validity = min(validity_days, 457)
    return (
        CertificateBuilder()
        .subject_name(LEAF_NAME)
        .issuer_name(ISSUER_NAME)
        .public_key(leaf_pub)
        .serial_number(x509.random_serial_number())
        .not_valid_before(now)
        .not_valid_after(now + timedelta(days=leaf_validity))
        .add_extension(
            x509.SubjectKeyIdentifier.from_public_key(leaf_pub), critical=False
        )
        .add_extension(
            x509.AuthorityKeyIdentifier.from_issuer_public_key(issuer_pub), critical=False
        )
        .add_extension(
            x509.KeyUsage(
                digital_signature=True,
                key_cert_sign=False, crl_sign=False,
                content_commitment=False, key_encipherment=False,
                data_encipherment=False, key_agreement=False,
                encipher_only=False, decipher_only=False
            ), critical=True
        )
        .add_extension(
            x509.ExtendedKeyUsage([OID_MDL_DS]), critical=True
        )
        .add_extension(
            x509.IssuerAlternativeName([
                x509.UniformResourceIdentifier("https://dvla.gov.uk/iaca")
            ]), critical=False
        )
        .sign(issuer_private_key, hashes.SHA256())
    )


def generate():
    args = get_argument_parser()

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

    issuer_private_key = get_issuer_private_key(args.issuer_private_key)

    now = datetime.now(timezone.utc)

    # Generate leaf signing key (separate from issuer root key)
    leaf_private_key = ec.generate_private_key(ec.SECP256R1())

    # Generate root CA certificate (trust anchor for verifier)
    root_cert = generate_root_certificate(issuer_private_key, now, args.validity_days)
    root_cert_der = root_cert.public_bytes(serialization.Encoding.DER)
    with open(args.x509_certificate, "wb") as f:
        f.write(root_cert_der)
    print(f"Generated root certificate: {args.x509_certificate}")

    # Also write PEM version
    pem_path = args.x509_certificate.replace(".der", ".pem")
    with open(pem_path, "wb") as f:
        f.write(root_cert.public_bytes(serialization.Encoding.PEM))
    print(f"Generated root certificate PEM: {pem_path}")

    # Generate leaf certificate (embedded in x5chain)
    leaf_cert = generate_leaf_certificate(
        leaf_private_key, issuer_private_key, root_cert, now, args.validity_days
    )
    leaf_cert_der = leaf_cert.public_bytes(serialization.Encoding.DER)

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
            "validUntil": cbor2.CBORTag(0, (now + timedelta(days=args.validity_days)).strftime(
                "%Y-%m-%dT%H:%M:%SZ")),
        },
    }

    mso_bytes = cbor2.dumps(mso)
    mso_tagged = cbor2.dumps(cbor2.CBORTag(24, mso_bytes))

    # Sign (COSE_Sign1) with the LEAF private key
    body_protected = cbor2.dumps({1: -7})
    sig_structure = cbor2.dumps(["Signature1", body_protected, b"", mso_tagged])
    signature_der = leaf_private_key.sign(sig_structure, ec.ECDSA(hashes.SHA256()))
    r, s = decode_dss_signature(signature_der)
    signature = r.to_bytes(32, "big") + s.to_bytes(32, "big")

    # x5chain contains the leaf cert (verifier has root as trust anchor)
    issuer_auth = [body_protected, {33: leaf_cert_der}, mso_tagged, signature]

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
    print(f"  Chain: root (trust anchor) -> leaf (in x5chain)")


def get_issuer_private_key(issuer_private_key_file_path: str):
    """
    Obtains an EC private key for use as the root CA key.
    """
    try:
        with open(issuer_private_key_file_path, "rb") as f:
            key = serialization.load_pem_private_key(f.read(), password=None)
            print(f"Loaded existing issuer private key: {issuer_private_key_file_path}")
            return key
    except FileNotFoundError:
        return generate_issuer_private_key(issuer_private_key_file_path)


def generate_issuer_private_key(issuer_private_key_file_path: str):
    """Generate and save a new EC private key."""
    print(f"'{issuer_private_key_file_path}' not found! Creating...")
    key = ec.generate_private_key(ec.SECP256R1())
    with open(issuer_private_key_file_path, "xb") as f:
        f.write(key.private_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PrivateFormat.PKCS8,
            encryption_algorithm=serialization.NoEncryption()
        ))
    print(f"Generated EC private key: {issuer_private_key_file_path}")
    return key


def get_argument_parser() -> IssuerAuthInput:
    parser = argparse.ArgumentParser(description="Generate a mock credential for the test app")
    parser.add_argument(
        "--private-key",
        help="Path to device private key PEM",
        default="app/src/main/assets/test_private_key.pem"
    )
    parser.add_argument(
        "--issuer-private-key",
        help="The private EC key for the root CA",
        default="app/src/main/assets/test_private_issuer_key.pem"
    )
    parser.add_argument(
        "--x509-certificate",
        help="Output path for the root CA certificate (DER format, trust anchor)",
        default="app/src/main/assets/test_x509_certificate.der"
    )
    parser.add_argument(
        "--output",
        help="Output path for credential txt file",
        default="app/src/main/res/raw/mock_credential.txt"
    )
    parser.add_argument(
        "--validity-days",
        type=int,
        default=365,
        help="Validity period in days (leaf capped at 457)"
    )
    args = parser.parse_args()

    return IssuerAuthInput(
        private_key = args.private_key,
        issuer_private_key = args.issuer_private_key,
        x509_certificate = args.x509_certificate,
        output = args.output,
        validity_days = args.validity_days
    )


if __name__ == "__main__":
    generate()
