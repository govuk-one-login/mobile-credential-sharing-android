#!/usr/bin/env python3
"""
Generates a mock_credential.txt for the Sharing SDK test app.

Requirements:
    pip install cbor2 cryptography

Usage:
    python3 scripts/mock_credential/generate_mock_credential.py \
    --issuer-private-key app/src/main/assets/test_private_issuer_key.pem \
    --private-key app/src/main/assets/test_private_key.pem \
    --x509-certificate app/src/main/assets/test_x509_certificate.pem \
    --output app/src/main/res/raw/mock_credential.txt \

The generated credential uses the device key from the provided PEM file and creates
a self-signed issuer certificate. The credential is valid for 1 year.
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
from cryptography.x509.oid import NameOID
from datetime import datetime, timezone, timedelta
from typing import Optional

# Sample IssuerSignedItems for a test mDL
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PORTRAIT_BYTES = base64.b64decode(
    open(os.path.join(SCRIPT_DIR, "portrait.txt")).read().strip()
)

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

    issuer_public_key = issuer_private_key.public_key()
    now = datetime.now(timezone.utc)
    try:
        cert = get_x509_certificate(args.x509_certificate)
    except FileNotFoundError as exception:
        print(f"'{args.x509_certificate}' not found! Creating...")
        cert = generate_x509_certificate(
            args.x509_certificate,
            args.validity_days,
            issuer_private_key,
            issuer_public_key,
            now
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
            "validUntil": cbor2.CBORTag(0, (now + timedelta(days=args.validity_days)).strftime(
                "%Y-%m-%dT%H:%M:%SZ")),
        },
    }

    mso_bytes = cbor2.dumps(mso)
    mso_tagged = cbor2.dumps(cbor2.CBORTag(24, mso_bytes))

    # Sign (COSE_Sign1)
    body_protected = cbor2.dumps({1: -7})
    sig_structure = cbor2.dumps(["Signature1", body_protected, b"", mso_tagged])
    signature_der = issuer_private_key.sign(sig_structure, ec.ECDSA(hashes.SHA256()))
    r, s = decode_dss_signature(signature_der)
    signature = r.to_bytes(32, "big") + s.to_bytes(32, "big")

    issuer_auth = [body_protected, {33: cert_der}, mso_tagged, signature]

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


def get_x509_certificate(x509_certificate_file_path: str) -> Optional[Certificate]:
    result = None
    with open(x509_certificate_file_path, "rb") as x509_certificate_file:
        result = x509.load_pem_x509_certificate(x509_certificate_file.read())
        print(f"Obtained existing X509 Certificate: {x509_certificate_file_path}")
    return result


def generate_x509_certificate(
        x509_certificate_file_path,
        validity_days,
        issuer_private_key,
        issuer_pub,
        now
) -> Certificate:
    result = (
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
        .not_valid_after(now + timedelta(days=validity_days))
        .add_extension(x509.SubjectKeyIdentifier.from_public_key(issuer_pub), critical=False)
        .add_extension(x509.BasicConstraints(ca=True, path_length=None), critical=True)
        .sign(issuer_private_key, hashes.SHA256())
    )

    with open(x509_certificate_file_path, "x") as f:
        f.write(
            result.public_bytes(serialization.Encoding.PEM,).decode("utf-8")
        )

    print(f"Generated X509 Certificate: {x509_certificate_file_path}")
    return result

def get_issuer_private_key(issuer_private_key_file_path: str) -> ec.EllipticCurve:
    """
    Obtains an EC private key for use in signing a mock credential.

    :param issuer_private_key_file_path: The file path of the Issuer private EC key to load. If this
           doesn't exist, the caught exception calls :func:`generate_issuer_private_key` to create
           a PEM file at this location.
    :return: The successfully obtains private EC key.
    """

    try:
        with open(issuer_private_key_file_path, "rb") as issuer_private_key_file:
            issuer_private_key = serialization.load_pem_private_key(
                issuer_private_key_file.read(),
                password=None
            )
            print(f"Loaded existing issuer private key: {issuer_private_key_file_path}")

    except FileNotFoundError as exception:
        issuer_private_key = generate_issuer_private_key(issuer_private_key_file_path)

    return issuer_private_key


def generate_issuer_private_key(issuer_private_key_file_path: str) -> ec.EllipticCurve:
    """
    :param issuer_private_key_file_path: The file path of the Issuer private key to generate.
    :return: The successfully generated EC private key, used for Issuer signing.
    """

    print(f"'{issuer_private_key_file_path}' not found! Creating...")
    issuer_private_key = ec.generate_private_key(ec.SECP256R1())
    with open(issuer_private_key_file_path, "x") as f:
        f.write(
            issuer_private_key.private_bytes(
                encoding=serialization.Encoding.PEM,
                format=serialization.PrivateFormat.PKCS8,
                encryption_algorithm=serialization.NoEncryption()
            ).decode("utf-8")
        )
        print(f"Generated EC private key: {issuer_private_key_file_path}")
    return issuer_private_key


def get_argument_parser() -> argparse.Namespace:
    """
    Obtains the command-line arguments necessary to use this script. These are:

    * `--private-key`
    * `--issuer-private-key`
    * `--output`
    * `--validity-days`

    The arguments are optional, with default values that may be overwritten at call-time.

    :return: The parsed arguments that this script requires
    """

    parser = argparse.ArgumentParser(description="Generate a mock credential for the test app")
    parser.add_argument(
        "--private-key",
        help="Path to device private key PEM",
        default="app/src/main/assets/test_private_key.pem"
    )
    parser.add_argument(
        "--issuer-private-key",
        help="The private EC key that issued the credential",
        default="app/src/main/assets/test_private_issuer_key.pem"
    )
    parser.add_argument(
        "--x509-certificate",
        help="The X509 Certificate file path, in PEM format",
        default="app/src/main/assets/test_x509_certificate.pem"
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
        help="Validity period in days"
    )
    return parser.parse_args()


# Prefer using `pipx install -e .` to install a symlinked `generate-mock-credential` command
if __name__ == "__main__":
    generate()
