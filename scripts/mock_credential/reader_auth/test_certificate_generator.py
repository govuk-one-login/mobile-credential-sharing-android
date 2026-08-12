from pytest import fixture
from cryptography import x509
from cryptography.x509 import Certificate
from cryptography.hazmat.primitives.asymmetric.ec import EllipticCurvePrivateKey
from datetime import datetime, timezone, timedelta

from mock_credential.certificates.generators import KeyGenerator
from mock_credential.certificates.generators.conftest import TEST_SUBJECT_NAME


class TestReaderAuthCertificateGenerator:
    @fixture
    def root_key(self) -> EllipticCurvePrivateKey:
        return KeyGenerator().generate()

    @fixture
    def valid_reader_auth_root_cert(
        self,
        reader_auth_cert_gen,
        root_key
    ) -> Certificate:
        return reader_auth_cert_gen.create_intermediate(
            private_key=root_key,
            subject=TEST_SUBJECT_NAME,
        )

    @fixture
    def reader_auth_now(self, reader_auth_cert_gen):
        return reader_auth_cert_gen.now

    def test_version_is_3(self, valid_reader_auth_root_cert: Certificate):
        assert valid_reader_auth_root_cert.version == x509.Version.v3

    def test_signature_is_ecdsa_sha_256(self, valid_reader_auth_root_cert):
        assert valid_reader_auth_root_cert.signature_algorithm_oid == x509.SignatureAlgorithmOID.ECDSA_WITH_SHA256

    def test_issuer_matches_subject(
        self,
        valid_reader_auth_root_cert: Certificate
    ):
        assert valid_reader_auth_root_cert.subject == TEST_SUBJECT_NAME
        assert valid_reader_auth_root_cert.issuer == valid_reader_auth_root_cert.subject

    def test_validity_not_before_matches_generator_property(
        self,
        valid_reader_auth_root_cert: Certificate,
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
        assert valid_reader_auth_root_cert.not_valid_before == expected
        assert valid_reader_auth_root_cert.not_valid_before_utc == expected.replace(
            tzinfo=timezone.utc
        )

    def test_validity_not_after_defaults_to_one_year(
        self,
        valid_reader_auth_root_cert: Certificate,
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
        assert valid_reader_auth_root_cert.not_valid_after == expected

    def test_certificate_has_common_name(
        self,
        valid_reader_auth_root_cert: Certificate
    ):
        assert x509.NameAttribute(x509.NameOID.COMMON_NAME, "Test Issuer") in valid_reader_auth_root_cert.subject

    def test_public_key_algorithm_is_id_ecPublicKey(
        self,
        valid_reader_auth_root_cert: Certificate
    ):
        assert valid_reader_auth_root_cert.public_key_algorithm_oid == x509.PublicKeyAlgorithmOID.EC_PUBLIC_KEY