#!/usr/bin/env python3
"""
Generates a mock_credential.txt for the Sharing SDK test app.
"""

import argparse
import os
import sys

# Add the 'scripts' directory to the path so we can import mock_credential
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from cryptography import x509
from cryptography.hazmat.primitives import serialization
from cryptography.x509.oid import ObjectIdentifier
from datetime import datetime, timezone

from mock_credential.issuer import IssuerAuthInput, ISSUER_NAME, LEAF_NAME
from mock_credential.namespaces import SAMPLE_NAMESPACES
from mock_credential.mso import MSO, SAMPLE_MSO
from mock_credential.certificates import CertificateGenerator, IssuerAuth, Credential, KeyGenerator

# ISO 18013-5 mdoc DS OID
OID_MDL_DS = ObjectIdentifier("1.0.18013.5.1.2")


def generate():
    args = get_argument_parser()
    now = datetime.now(timezone.utc)
    cert_gen = CertificateGenerator(now)

    # 1. Keys & Certificates
    issuer_key = KeyGenerator.load_or_create(args.issuer_private_key)
    root_cert = cert_gen.create_root_ca(issuer_key, ISSUER_NAME, validity_days=args.validity_days)

    leaf_key = KeyGenerator.generate()
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
    device_key = KeyGenerator.load(args.private_key)
    device_pub = device_key.public_key().public_numbers()
    device_cose_key = {1: 2, -1: 1, -2: device_pub.x.to_bytes(32, "big"), -3: device_pub.y.to_bytes(32, "big")}

    # 3. Assemble MSO & Credential
    mso_obj = MSO.from_dict(SAMPLE_MSO, device_cose_key, validity_days=args.validity_days)
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
