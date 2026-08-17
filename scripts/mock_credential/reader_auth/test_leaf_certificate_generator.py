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
    ExtensionType
)
from cryptography.x509.oid import (
    AuthorityInformationAccessOID
)
from cryptography.hazmat.primitives.asymmetric.ec import EllipticCurvePrivateKey
from datetime import datetime, timezone
from typing import (
    Generator,
    List,
    Tuple
)

from mock_credential.certificates.generators import PemKeyGenerator
from mock_credential.certificates.generators.conftest import TEST_SUBJECT_NAME
from mock_credential.reader_auth import ReaderAuthCertificateGenerator

# mdlReaderAuth
OID_MDL_RA = ObjectIdentifier("1.0.18013.5.1.6")
# mdocReaderAuth
OID_MDOC_RA = ObjectIdentifier("1.0.23220.4.1.6")

class TestReaderAuthLeafCertificateGenerator:
    @fixture
    def root_key(self) -> EllipticCurvePrivateKey:
        return PemKeyGenerator().generate()

    @fixture
    def leaf_key(self) -> EllipticCurvePrivateKey:
        return PemKeyGenerator().generate()

    @fixture
    def valid_intermediate_cert(
        self,
        reader_auth_cert_gen,
        root_key
    ) -> Certificate:
        return reader_auth_cert_gen.create_intermediate(
            private_key=root_key,
            subject=TEST_SUBJECT_NAME,
        )

    @fixture
    def leaf_certificate_extensions_input(
        self
    ) -> List[Tuple[ExtensionType, bool]]:
        return []

    @fixture
    def subject_name(self) -> Name:
        return Name(
            [
                NameAttribute(NameOID.COUNTRY_NAME, "GB"),
                NameAttribute(NameOID.STATE_OR_PROVINCE_NAME, "London"),
                NameAttribute(NameOID.COMMON_NAME, "mDoc Test Leaf"),
                NameAttribute(NameOID.ORGANIZATION_NAME, "DVLA Dev Tool"),
            ]
        )

    @fixture
    def valid_leaf_certificate(
        self,
        valid_intermediate_cert: Certificate,
        subject_name: Name,
        root_key: EllipticCurvePrivateKey,
        leaf_key: EllipticCurvePrivateKey,
        reader_auth_cert_gen: ReaderAuthCertificateGenerator,
        leaf_certificate_extensions_input: List[Tuple[ExtensionType, bool]]
    ) -> Certificate:
        return reader_auth_cert_gen.create_certificate(
            leaf_key.public_key(),
            root_key,
            subject_name,
            valid_intermediate_cert,
            extensions=leaf_certificate_extensions_input + [
                (
                    SubjectInformationAccess(
                        [
                            AccessDescription(
                                access_method=ObjectIdentifier(
                                    "1.3.6.1.4.1.72548.1.1",
                                ),
                                access_location=UniformResourceIdentifier(
                                    "https://www.gov.uk/"
                                )
                            )
                        ]
                    ),
                    False
                )
            ],
        )

    @fixture
    def invalid_leaf_certificate(
        self,
        valid_intermediate_cert: Certificate,
        subject_name: Name,
        root_key: EllipticCurvePrivateKey,
        leaf_key: EllipticCurvePrivateKey,
        reader_auth_cert_gen: ReaderAuthCertificateGenerator,
        leaf_certificate_extensions_input: List[Tuple[ExtensionType, bool]]
    ) -> Certificate:
        return reader_auth_cert_gen.create_certificate(
            subject_key=leaf_key.public_key(),
            issuer_key=root_key,
            subject_name=subject_name,
            issuer_cert=valid_intermediate_cert,
            extensions=leaf_certificate_extensions_input,
        )

    @fixture(
        params=[
            "valid_leaf_certificate",
            "invalid_leaf_certificate"
        ]
    )
    def leaf_certificates(
        self,
        request
    ) -> Generator[Certificate, None, None]:
        return request.getfixturevalue(request.param)

    @fixture
    def reader_auth_now(self, reader_auth_cert_gen):
        return reader_auth_cert_gen.now

    def test_version_is_3(self, leaf_certificates: Certificate):
        assert leaf_certificates.version == Version.v3
