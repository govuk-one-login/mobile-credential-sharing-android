from pytest import fixture
from cryptography.hazmat.primitives.asymmetric.ec import EllipticCurvePrivateKey
from cryptography.x509 import (
    ExtendedKeyUsage,
    IssuerAlternativeName,
    KeyUsage,
    Name,
    NameAttribute,
    UniformResourceIdentifier,
)
from cryptography.x509.oid import NameOID, ObjectIdentifier

from mock_credential.certificates.generators import (
    KeyGenerator,
)

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
def key_gen() -> KeyGenerator:
    return KeyGenerator()

@fixture
def root_key(key_gen: KeyGenerator) -> EllipticCurvePrivateKey:
    return key_gen.generate()

@fixture
def leaf_key(key_gen: KeyGenerator) -> EllipticCurvePrivateKey:
    return key_gen.generate()
