from pytest import fixture
from cryptography.hazmat.primitives.asymmetric.ec import EllipticCurvePrivateKey
from cryptography.x509 import (
    Certificate,
    ExtendedKeyUsage,
    IssuerAlternativeName,
    KeyUsage,
    Name,
    NameAttribute,
    UniformResourceIdentifier,
)
from cryptography.x509.oid import NameOID, ObjectIdentifier
from typing import Generator

from mock_credential.certificates.generators import (
    CertificateGenerator,
)
from mock_credential.certificates.generators.conftest import root_key, leaf_key
from mock_credential.issuer_auth import IssuerAuthCertificateGenerator

TEST_SUBJECT_NAME = Name(
    [
        NameAttribute(NameOID.COUNTRY_NAME, "GB"),
        NameAttribute(NameOID.COMMON_NAME, "Test Issuer"),
    ]
)

TEST_LEAF_NAME = Name(
    [
        NameAttribute(NameOID.COUNTRY_NAME, "GB"),
        NameAttribute(NameOID.COMMON_NAME, "Test Leaf"),
    ]
)

# ISO 18013-5 mdoc DS OID — same as used in generate_mock_credential.py
OID_MDL_DS = ObjectIdentifier("1.0.18013.5.1.2")

LEAF_EXTENSIONS = [
    (
        KeyUsage(
            digital_signature=True,
            content_commitment=False,
            key_encipherment=False,
            data_encipherment=False,
            key_agreement=False,
            key_cert_sign=False,
            crl_sign=False,
            encipher_only=False,
            decipher_only=False,
        ),
        True,
    ),
    (ExtendedKeyUsage([OID_MDL_DS]), True),
    (
        IssuerAlternativeName([UniformResourceIdentifier("https://dvla.gov.uk/iaca")]),
        False,
    ),
]


@fixture
def issuer_auth_cert_gen() -> CertificateGenerator:
    return IssuerAuthCertificateGenerator()


@fixture
def issuer_auth_now(issuer_auth_cert_gen):
    return issuer_auth_cert_gen.now


@fixture
def root_cert(
    issuer_auth_cert_gen: CertificateGenerator, root_key: EllipticCurvePrivateKey
) -> Certificate:
    return issuer_auth_cert_gen.create_intermediate(root_key, TEST_SUBJECT_NAME)


@fixture
def leaf_cert(
    issuer_auth_cert_gen: CertificateGenerator,
    leaf_key: EllipticCurvePrivateKey,
    root_key: EllipticCurvePrivateKey,
    root_cert: Certificate,
) -> Generator[Certificate, None, None]:
    yield issuer_auth_cert_gen.create_certificate(
        leaf_key.public_key(),
        root_key,
        TEST_LEAF_NAME,
        root_cert,
        validity_days=365,
        extensions=LEAF_EXTENSIONS,
    )
