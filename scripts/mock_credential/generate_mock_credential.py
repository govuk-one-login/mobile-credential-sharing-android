#!/usr/bin/env python3
"""
Generates a mock_credential.txt for the Sharing SDK test app.
Refactored to use modular components.
"""

import argparse
import base64
import cbor2
import os
import sys

# Add the 'scripts' directory to the path so we can import mock_credential
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives.asymmetric.utils import decode_dss_signature
from cryptography.x509.oid import ExtendedKeyUsageOID, ObjectIdentifier
from datetime import datetime, timezone, timedelta
from typing import Dict

from mock_credential.issuer import IssuerAuthInput
from mock_credential.namespaces import SAMPLE_NAMESPACES
from mock_credential.mso import MSO
from mock_credential.certificate_generator import CertificateGenerator
from mock_credential.credential import IssuerAuth, Credential

# ISO 18013-5 OIDs
OID_MDL_DS = ObjectIdentifier("1.0.18013.5.1.2")

ISSUER_NAME = x509.Name([
    x509.NameAttribute(x509.NameOID.COUNTRY_NAME, "GB"),
    x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME, "London"),
    x509.NameAttribute(x509.NameOID.COMMON_NAME, "mDoc Test Issuer"),
    x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, "DVLA Dev Tool"),
])

LEAF_NAME = x509.Name([
    x509.NameAttribute(x509.NameOID.COUNTRY_NAME, "GB"),
    x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME, "London"),
    x509.NameAttribute(x509.NameOID.COMMON_NAME, "mDoc Test Leaf"),
    x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, "DVLA Dev Tool"),
])

def get_issuer_private_key(path: str) -> ec.EllipticCurvePrivateKey:
    if os.path.exists(path):
        with open(path, "rb") as f:
            return serialization.load_pem_private_key(f.read(), password=None)

    print(f"'{path}' not found! Creating...")
    key = ec.generate_private_key(ec.SECP256R1())
    with open(path, "xb") as f:
        f.write(key.private_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PrivateFormat.PKCS8,
            encryption_algorithm=serialization.NoEncryption()
        ))
    return key

def generate():
    args = get_argument_parser()
    now = datetime.now(timezone.utc)
    cert_gen = CertificateGenerator(now)

    # 1. Keys & Certificates
    issuer_key = get_issuer_private_key(args.issuer_private_key)
    root_cert = cert_gen.create_root_ca(issuer_key, ISSUER_NAME, validity_days=args.validity_days)

    leaf_key = cert_gen.generate_key()
    leaf_cert = cert_gen.create_certificate(
        leaf_key.public_key(), issuer_key, LEAF_NAME, root_cert,
        validity_days=min(args.validity_days, 457),
        extensions=[
            (x509.KeyUsage(digital_signature=True, content_commitment=False, key_encipherment=False,
                          data_encipherment=False, key_agreement=False, key_cert_sign=False,
                          crl_sign=False, encipher_only=False, decipher_only=False), True),
            (x509.ExtendedKeyUsage([OID_MDL_DS]), True),
            (x509.IssuerAlternativeName([x509.UniformResourceIdentifier("https://dvla.gov.uk/iaca")]), False),
        ]
    )

    # 2. Device Key
    with open(args.private_key, "rb") as f:
        device_key = serialization.load_pem_private_key(f.read(), password=None)
    device_pub = device_key.public_key().public_numbers()
    device_cose_key = {1: 2, -1: 1, -2: device_pub.x.to_bytes(32, "big"), -3: device_pub.y.to_bytes(32, "big")}

    # 3. Assemble MSO & Credential
    mso_obj = MSO(SAMPLE_NAMESPACES, device_cose_key, validity_days=args.validity_days)
    mso_tagged_bytes = mso_obj.build_tagged_bytes(now)

    issuer_auth_obj = IssuerAuth(mso_tagged_bytes, leaf_cert.public_bytes(serialization.Encoding.DER))
    issuer_auth_list = issuer_auth_obj.sign(leaf_key)

    credential = Credential(SAMPLE_NAMESPACES.build_issuer_signed_items(), issuer_auth_list)

    # 4. Save Outputs
    with open(args.x509_certificate, "wb") as f:
        f.write(root_cert.public_bytes(serialization.Encoding.DER))

    with open(args.output, "w") as f:
        f.write(credential.to_base64url())

    print(f"Generated: {args.output}")
    print(f"Root Certificate: {args.x509_certificate}")

def get_argument_parser() -> IssuerAuthInput:
    parser = argparse.ArgumentParser(description="Generate a mock credential")
    parser.add_argument("--private-key", default="app/src/main/assets/test_private_key.pem")
    parser.add_argument("--issuer-private-key", default="app/src/main/assets/test_private_issuer_key.pem")
    parser.add_argument("--x509-certificate", default="app/src/main/assets/test_x509_certificate.der")
    parser.add_argument("--output", default="app/src/main/res/raw/mock_credential.txt")
    parser.add_argument("--validity-days", type=int, default=365)
    return IssuerAuthInput.from_parser(parser.parse_args())

if __name__ == "__main__":
    generate()
