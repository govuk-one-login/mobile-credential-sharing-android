from pytest import fixture
from cryptography.x509 import (
    Certificate,
    Extensions,
    BasicConstraints,
    KeyUsage,
    ExtendedKeyUsage,
    Version,
    SignatureAlgorithmOID,
    NameOID,
    NameAttribute,
    PublicKeyAlgorithmOID
)
from cryptography.hazmat.primitives.asymmetric.ec import EllipticCurvePrivateKey
from datetime import datetime, timezone

from mock_credential.certificates.generators import PemKeyGenerator
from mock_credential.certificates.generators.conftest import TEST_SUBJECT_NAME


class TestReaderAuthCertificateGenerator:
    @fixture
    def root_key(self) -> EllipticCurvePrivateKey:
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
    def valid_intermediate_extensions(
        self,
        valid_intermediate_cert: Certificate
    ) -> Extensions:
        return valid_intermediate_cert.extensions

    @fixture
    def reader_auth_now(self, reader_auth_cert_gen):
        return reader_auth_cert_gen.now

    def test_version_is_3(self, valid_intermediate_cert: Certificate):
        assert valid_intermediate_cert.version == Version.v3

    def test_signature_is_ecdsa_sha_256(self, valid_intermediate_cert):
        assert valid_intermediate_cert.signature_algorithm_oid == SignatureAlgorithmOID.ECDSA_WITH_SHA256

    def test_issuer_matches_subject(
        self,
        valid_intermediate_cert: Certificate
    ):
        assert valid_intermediate_cert.subject == TEST_SUBJECT_NAME
        assert valid_intermediate_cert.issuer == valid_intermediate_cert.subject

    def test_validity_not_before_matches_generator_property(
        self,
        valid_intermediate_cert: Certificate,
        reader_auth_now
    ):
        expected = datetime(
            year=reader_auth_now.year,
            month=reader_auth_now.month,
            day=reader_auth_now.day,
            hour=reader_auth_now.hour,
            minute=reader_auth_now.minute,
            second=reader_auth_now.second,
        )
        assert valid_intermediate_cert.not_valid_before == expected
        assert valid_intermediate_cert.not_valid_before_utc == expected.replace(
            tzinfo=timezone.utc
        )

    def test_validity_not_after_defaults_to_one_year(
        self,
        valid_intermediate_cert: Certificate,
        reader_auth_now
    ):
        expected = datetime(
            year=reader_auth_now.year + 1,
            month=reader_auth_now.month,
            day=reader_auth_now.day,
            hour=reader_auth_now.hour,
            minute=reader_auth_now.minute,
            second=reader_auth_now.second,
        )
        assert valid_intermediate_cert.not_valid_after == expected

    def test_certificate_has_common_name(
        self,
        valid_intermediate_cert: Certificate
    ):
        assert NameAttribute(NameOID.COMMON_NAME, "Test Issuer") in valid_intermediate_cert.subject

    def test_public_key_algorithm_is_id_ecPublicKey(
        self,
        valid_intermediate_cert: Certificate
    ):
        assert valid_intermediate_cert.public_key_algorithm_oid == PublicKeyAlgorithmOID.EC_PUBLIC_KEY
    
    def test_basic_constraints_is_critical(self, valid_intermediate_extensions: Extensions):
        bc = valid_intermediate_extensions.get_extension_for_class(BasicConstraints)
        assert bc.critical

    def test_key_usage_key_cert_sign_is_true(self, valid_intermediate_extensions: Extensions):
        ku = valid_intermediate_extensions.get_extension_for_class(KeyUsage)
        assert ku.value.key_cert_sign

    def test_key_usage_is_critical(self, valid_intermediate_extensions: Extensions):
        ku = valid_intermediate_extensions.get_extension_for_class(KeyUsage)
        assert ku.critical
