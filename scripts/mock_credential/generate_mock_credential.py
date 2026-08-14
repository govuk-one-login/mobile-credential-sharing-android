#!/usr/bin/env python3
"""
Generates a mock_credential.txt for the Sharing SDK test app.
"""

import argparse
import logging
from logging518 import config as logging_config
import os
import sys


# Add the 'scripts' directory to the path so we can import mock_credential
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from cryptography.hazmat.primitives import serialization
from cryptography.x509.oid import ObjectIdentifier
from datetime import datetime, timezone

from mock_credential import GenerateMockCredentialInputs
from mock_credential.namespaces import SAMPLE_NAMESPACES
from mock_credential.mso import MSO, SAMPLE_MSO
from mock_credential.certificates import (
    IssuerAuth,
    Credential,
)
from mock_credential.certificates.generators import (
    PemKeyGenerator,
    DerKeyGenerator
)
from mock_credential.issuer_auth import IssuerAuthCertificateGenerator
from mock_credential.reader_auth import ReaderAuthCertificateGenerator

logging_config.fileConfig("pyproject.toml")
logger = logging.getLogger("project")

def generate():
    args = get_argument_parser()
    now = datetime.now(timezone.utc)
    pem_key_gen = PemKeyGenerator()
    der_key_gen = DerKeyGenerator()
    cert_gen = IssuerAuthCertificateGenerator(now=now)
    reader_cert_gen = ReaderAuthCertificateGenerator(now=now)

    # 1. Keys & Certificates
    issuer_leaf_key, issuer_leaf_cert = args.create_issuer_auth_certificates(
        cert_gen=cert_gen,
        key_gen=pem_key_gen,
    )
    valid_reader_leaf_cert, invalid_reader_leaf_cert = args.create_reader_auth_certificates(
        cert_gen=reader_cert_gen,
        root_key_gen=pem_key_gen,
        intermediate_key_gen=der_key_gen
    )

    # 2. Device Key
    device_key = pem_key_gen.load(args.private_key)
    device_pub = device_key.public_key().public_numbers()
    device_cose_key = {
        1: 2,
        -1: 1,
        -2: device_pub.x.to_bytes(32, "big"),
        -3: device_pub.y.to_bytes(32, "big"),
    }

    # 3. Assemble MSO & Credential
    mso_obj = MSO.from_dict(SAMPLE_MSO, device_cose_key, validity_days=args.validity_days)
    mso_tagged_bytes = mso_obj.build_tagged_bytes(now)

    issuer_auth_obj = IssuerAuth(
        mso_tagged_bytes, issuer_leaf_cert.public_bytes(serialization.Encoding.DER)
    )
    issuer_auth_list = issuer_auth_obj.sign(issuer_leaf_key)

    credential = Credential(SAMPLE_NAMESPACES.build_issuer_signed_items(), issuer_auth_list)

    # 4. Save Outputs
    with open(args.output, "w") as f:
        f.write(credential.to_base64url())

    logger.info(f"Generated: {args.output}")


def get_argument_parser() -> GenerateMockCredentialInputs:
    parser = argparse.ArgumentParser(description="Generate a mock credential")
    parser.add_argument("--private-key", default="app/src/main/assets/test_private_key.pem")
    parser.add_argument(
        "--issuer-private-key",
        default="app/src/main/assets/test_private_issuer_key.pem",
    )
    parser.add_argument(
        "--reader-auth-private-key", default="app/src/main/assets/test_private_reader_auth_key.pem"
    )
    parser.add_argument(
        "--issuer-intermediate-x509-certificate",
        default="app/src/main/assets/test_x509_certificate.der"
    )
    parser.add_argument(
        "--reader-intermediate-x509-certificate",
        default="app/src/main/assets/test_reader_auth_x509_certificate.der"
    )
    parser.add_argument("--output", default="app/src/main/res/raw/mock_credential.txt")
    parser.add_argument("--validity-days", type=int, default=365)
    parser.add_argument(
        "--reader-valid-x509-leaf-certificate",
        default="app/src/main/assets/reader_valid_x509_leaf_certificate.der"
    )
    parser.add_argument(
        "--reader-invalid-x509-leaf-certificate",
        default="app/src/main/assets/reader_invalid_x509_leaf_certificate.der"
    )

    logger.info("Obtained command-line arguments...")
    return GenerateMockCredentialInputs.from_parser(parser.parse_args())


if __name__ == "__main__":
    generate()
