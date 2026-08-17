from pytest import fixture, raises
import pytest
from cryptography.x509 import (
    AccessDescription,
    AuthorityInformationAccess,
    Certificate,
    Extension,
    ExtensionNotFound,
    Extensions,
    AuthorityKeyIdentifier,
    BasicConstraints,
    KeyUsage,
    ExtendedKeyUsage,
    Name,
    Version,
    SignatureAlgorithmOID,
    NameOID,
    NameAttribute,
    PublicKeyAlgorithmOID,
    SubjectInformationAccess,
    SubjectKeyIdentifier,
    ObjectIdentifier,
    UniformResourceIdentifier,
    ExtensionType,
)
from cryptography.x509.oid import AuthorityInformationAccessOID
from cryptography.hazmat.primitives.asymmetric.ec import EllipticCurvePrivateKey
from typing import Generator, List, Tuple

from mock_credential.certificates.generators import PemKeyGenerator
from mock_credential.certificates.generators.conftest import TEST_SUBJECT_NAME
from mock_credential.reader_auth import (
    ReaderAuthCertificateGenerator,
    READER_AUTH_COMMON_LEAF_EXTENSIONS,
    READER_AUTH_LEAF_SUBJECT_NAME,
)


@fixture
def reader_auth_cert_gen():
    return ReaderAuthCertificateGenerator()


@fixture
def pem_key_generator() -> PemKeyGenerator:
    return PemKeyGenerator()


@fixture
def root_key(pem_key_generator: PemKeyGenerator) -> EllipticCurvePrivateKey:
    return pem_key_generator.generate()


@fixture
def valid_leaf_key(pem_key_generator: PemKeyGenerator) -> EllipticCurvePrivateKey:
    return pem_key_generator.generate()


@fixture
def invalid_leaf_key(pem_key_generator: PemKeyGenerator) -> EllipticCurvePrivateKey:
    return pem_key_generator.generate()


@fixture
def valid_intermediate_cert(reader_auth_cert_gen, root_key) -> Certificate:
    return reader_auth_cert_gen.create_intermediate(
        private_key=root_key,
        subject=TEST_SUBJECT_NAME,
    )


@fixture
def valid_intermediate_extensions(valid_intermediate_cert: Certificate) -> Extensions:
    return valid_intermediate_cert.extensions


@fixture
def valid_intermediate_key_usage(valid_intermediate_extensions: Extensions) -> Extension[KeyUsage]:
    return valid_intermediate_extensions.get_extension_for_class(KeyUsage)


@fixture
def leaf_certificate_extensions_input() -> List[Tuple[ExtensionType, bool]]:
    return READER_AUTH_COMMON_LEAF_EXTENSIONS


@fixture
def subject_name() -> Name:
    return READER_AUTH_LEAF_SUBJECT_NAME


@fixture
def valid_leaf_certificate(
    valid_intermediate_cert: Certificate,
    subject_name: Name,
    root_key: EllipticCurvePrivateKey,
    valid_leaf_key: EllipticCurvePrivateKey,
    reader_auth_cert_gen: ReaderAuthCertificateGenerator,
    leaf_certificate_extensions_input: List[Tuple[ExtensionType, bool]],
) -> Certificate:
    return reader_auth_cert_gen.create_certificate(
        valid_leaf_key.public_key(),
        root_key,
        subject_name,
        valid_intermediate_cert,
        extensions=leaf_certificate_extensions_input
        + [
            (
                SubjectInformationAccess(
                    [
                        AccessDescription(
                            access_method=ObjectIdentifier(
                                "1.3.6.1.4.1.72548.1.1",
                            ),
                            access_location=UniformResourceIdentifier("https://www.gov.uk/"),
                        )
                    ]
                ),
                False,
            )
        ],
    )


@fixture
def invalid_leaf_certificate(
    valid_intermediate_cert: Certificate,
    subject_name: Name,
    root_key: EllipticCurvePrivateKey,
    invalid_leaf_key: EllipticCurvePrivateKey,
    reader_auth_cert_gen: ReaderAuthCertificateGenerator,
    leaf_certificate_extensions_input: List[Tuple[ExtensionType, bool]],
) -> Certificate:
    return reader_auth_cert_gen.create_certificate(
        subject_key=invalid_leaf_key.public_key(),
        issuer_key=root_key,
        subject_name=subject_name,
        issuer_cert=valid_intermediate_cert,
        extensions=leaf_certificate_extensions_input,
    )


@fixture(params=["valid_leaf_certificate", "invalid_leaf_certificate"])
def leaf_certificates(request) -> Generator[Certificate, None, None]:
    return request.getfixturevalue(request.param)


@fixture
def leaf_certificate_extensions(leaf_certificates: Certificate) -> Extensions:
    return leaf_certificates.extensions


@fixture
def leaf_key_usage(leaf_certificate_extensions: Extensions) -> Extension[KeyUsage]:
    return leaf_certificate_extensions.get_extension_for_class(KeyUsage)


@fixture
def reader_auth_now(reader_auth_cert_gen):
    return reader_auth_cert_gen.now
