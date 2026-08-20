import pytest
from cryptography import x509
from cryptography.x509 import Certificate
from datetime import datetime, timedelta, timezone

from mock_credential.certificates.generators.conftest import (
    LEAF_EXTENSIONS,
    TEST_LEAF_NAME,
    TEST_SUBJECT_NAME,
    key_gen,
)


class TestCreateRootCa:

    def test_returns_certificate(self, issuer_auth_cert_gen, root_key):
        cert = issuer_auth_cert_gen.create_intermediate(root_key, TEST_SUBJECT_NAME)
        assert isinstance(cert, Certificate)

    def test_is_self_signed(self, root_cert):
        assert root_cert.subject == root_cert.issuer

    def test_subject_matches_provided_name(self, root_cert):
        assert root_cert.subject == TEST_SUBJECT_NAME

    def test_basic_constraints_ca_is_true(self, root_cert):
        bc = root_cert.extensions.get_extension_for_class(x509.BasicConstraints)
        assert bc.value.ca is True

    def test_basic_constraints_is_critical(self, root_cert):
        bc = root_cert.extensions.get_extension_for_class(x509.BasicConstraints)
        assert bc.critical is True

    def test_key_usage_key_cert_sign_is_true(self, root_cert):
        ku = root_cert.extensions.get_extension_for_class(x509.KeyUsage)
        assert ku.value.key_cert_sign is True

    def test_key_usage_is_critical(self, root_cert):
        ku = root_cert.extensions.get_extension_for_class(x509.KeyUsage)
        assert ku.critical is True

    def test_validity_not_before_uses_provided_now(
        self, issuer_auth_cert_gen, root_key, issuer_auth_now
    ):
        expected = datetime(
            year=issuer_auth_now.year,
            month=issuer_auth_now.month,
            day=issuer_auth_now.day,
            hour=issuer_auth_now.hour,
            minute=issuer_auth_now.minute,
            second=issuer_auth_now.second,
            tzinfo=timezone.utc,
        )
        cert = issuer_auth_cert_gen.create_intermediate(
            root_key, TEST_SUBJECT_NAME, validity_days=365
        )
        assert cert.not_valid_before_utc == expected

    def test_validity_not_after_applies_validity_days(
        self, issuer_auth_cert_gen, root_key, issuer_auth_now
    ):
        expected = datetime(
            year=issuer_auth_now.year + 1,
            month=issuer_auth_now.month,
            day=issuer_auth_now.day,
            hour=issuer_auth_now.hour,
            minute=issuer_auth_now.minute,
            second=issuer_auth_now.second,
            tzinfo=timezone.utc,
        )
        cert = issuer_auth_cert_gen.create_intermediate(
            root_key, TEST_SUBJECT_NAME, validity_days=365
        )
        assert cert.not_valid_after_utc == expected


class TestCreateCertificate:

    def test_returns_certificate(self, leaf_cert):
        assert isinstance(leaf_cert, Certificate)

    def test_subject_matches_provided_name(self, leaf_cert):
        assert leaf_cert.subject == TEST_LEAF_NAME

    def test_issuer_matches_root_cert_subject(self, leaf_cert, root_cert):
        assert leaf_cert.issuer == root_cert.subject

    def test_key_usage_digital_signature_is_true(self, leaf_cert):
        ku = leaf_cert.extensions.get_extension_for_class(x509.KeyUsage)
        assert ku.value.digital_signature is True

    def test_key_usage_is_critical(self, leaf_cert):
        """Regression: was silently non-critical before the (ext, critical) tuple fix."""
        ku = leaf_cert.extensions.get_extension_for_class(x509.KeyUsage)
        assert ku.critical is True

    def test_extended_key_usage_is_critical(self, leaf_cert):
        """Regression: was silently non-critical before the (ext, critical) tuple fix."""
        eku = leaf_cert.extensions.get_extension_for_class(x509.ExtendedKeyUsage)
        assert eku.critical is True

    def test_non_critical_extension_is_not_critical(self, leaf_cert):
        ian = leaf_cert.extensions.get_extension_for_class(x509.IssuerAlternativeName)
        assert ian.critical is False

    def test_validity_not_after_applies_validity_days(
        self, issuer_auth_cert_gen, leaf_key, root_key, root_cert, issuer_auth_now
    ):
        expected = datetime(
            year=issuer_auth_now.year,
            month=issuer_auth_now.month,
            day=issuer_auth_now.day,
            hour=issuer_auth_now.hour,
            minute=issuer_auth_now.minute,
            second=issuer_auth_now.second,
            tzinfo=timezone.utc,
        )
        cert = issuer_auth_cert_gen.create_certificate(
            leaf_key.public_key(),
            root_key,
            TEST_LEAF_NAME,
            root_cert,
            validity_days=100,
            extensions=LEAF_EXTENSIONS,
        )
        assert cert.not_valid_after_utc == expected + timedelta(days=100)
