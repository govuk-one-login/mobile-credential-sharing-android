import pytest
from cryptography import x509
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.x509 import Certificate, Name, NameAttribute
from cryptography.x509.oid import NameOID, ObjectIdentifier
from datetime import datetime, timezone
from typing import Generator

from mock_credential.certificates import CertificateGenerator, KeyGenerator

# ISO 18013-5 mdoc DS OID — same as used in generate_mock_credential.py
OID_MDL_DS = ObjectIdentifier("1.0.18013.5.1.2")

TEST_SUBJECT_NAME = Name([
    NameAttribute(NameOID.COUNTRY_NAME, "GB"),
    NameAttribute(NameOID.COMMON_NAME, "Test Issuer"),
])

TEST_LEAF_NAME = Name([
    NameAttribute(NameOID.COUNTRY_NAME, "GB"),
    NameAttribute(NameOID.COMMON_NAME, "Test Leaf"),
])

LEAF_EXTENSIONS = [
    (x509.KeyUsage(
        digital_signature=True, content_commitment=False, key_encipherment=False,
        data_encipherment=False, key_agreement=False, key_cert_sign=False,
        crl_sign=False, encipher_only=False, decipher_only=False
    ), True),
    (x509.ExtendedKeyUsage([OID_MDL_DS]), True),
    (x509.IssuerAlternativeName([x509.UniformResourceIdentifier("https://dvla.gov.uk/iaca")]), False),
]


@pytest.fixture
def now() -> datetime:
    return datetime(2025, 1, 1, tzinfo=timezone.utc)


@pytest.fixture
def cert_gen(now: datetime) -> CertificateGenerator:
    return CertificateGenerator(now)


@pytest.fixture
def root_key() -> ec.EllipticCurvePrivateKey:
    return KeyGenerator.generate()


@pytest.fixture
def root_cert(cert_gen: CertificateGenerator, root_key: ec.EllipticCurvePrivateKey) -> Certificate:
    return cert_gen.create_root_ca(root_key, TEST_SUBJECT_NAME)


@pytest.fixture
def leaf_key() -> ec.EllipticCurvePrivateKey:
    return KeyGenerator.generate()


@pytest.fixture
def leaf_cert(
    cert_gen: CertificateGenerator,
    leaf_key: ec.EllipticCurvePrivateKey,
    root_key: ec.EllipticCurvePrivateKey,
    root_cert: Certificate
) -> Generator[Certificate, None, None]:
    yield cert_gen.create_certificate(
        leaf_key.public_key(), root_key, TEST_LEAF_NAME, root_cert,
        validity_days=365,
        extensions=LEAF_EXTENSIONS
    )
