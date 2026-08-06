import pytest
from cryptography import x509
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.x509 import Certificate
from datetime import timedelta

from mock_credential.certificates import CertificateGenerator, KeyGenerator
from .conftest import TEST_SUBJECT_NAME, TEST_LEAF_NAME, LEAF_EXTENSIONS


class TestGenerateKey:

    def test_returns_ec_private_key(self):
        key = KeyGenerator.generate()
        assert isinstance(key, ec.EllipticCurvePrivateKey)

    def test_uses_p256_curve(self):
        key = KeyGenerator.generate()
        assert isinstance(key.curve, ec.SECP256R1)

    def test_each_call_produces_unique_key(self):
        key_a = KeyGenerator.generate()
        key_b = KeyGenerator.generate()
        assert key_a.private_numbers() != key_b.private_numbers()


class TestKeyGeneratorLoad:

    def test_load_returns_private_key(self, tmp_path):
        key = KeyGenerator.generate()
        path = str(tmp_path / "key.pem")
        with open(path, "xb") as f:
            from cryptography.hazmat.primitives import serialization
            f.write(key.private_bytes(
                encoding=serialization.Encoding.PEM,
                format=serialization.PrivateFormat.PKCS8,
                encryption_algorithm=serialization.NoEncryption()
            ))
        loaded = KeyGenerator.load(path)
        assert isinstance(loaded, ec.EllipticCurvePrivateKey)

    def test_load_returns_same_key(self, tmp_path):
        key = KeyGenerator.generate()
        path = str(tmp_path / "key.pem")
        with open(path, "xb") as f:
            from cryptography.hazmat.primitives import serialization
            f.write(key.private_bytes(
                encoding=serialization.Encoding.PEM,
                format=serialization.PrivateFormat.PKCS8,
                encryption_algorithm=serialization.NoEncryption()
            ))
        loaded = KeyGenerator.load(path)
        assert loaded.private_numbers() == key.private_numbers()

    def test_load_raises_when_file_missing(self, tmp_path):
        with pytest.raises(FileNotFoundError):
            KeyGenerator.load(str(tmp_path / "missing.pem"))


class TestKeyGeneratorLoadOrCreate:

    def test_creates_key_when_file_missing(self, tmp_path):
        path = str(tmp_path / "new_key.pem")
        key = KeyGenerator.load_or_create(path)
        assert isinstance(key, ec.EllipticCurvePrivateKey)

    def test_saves_key_to_disk_when_created(self, tmp_path):
        path = str(tmp_path / "new_key.pem")
        KeyGenerator.load_or_create(path)
        assert (tmp_path / "new_key.pem").exists()

    def test_loads_existing_key_when_file_present(self, tmp_path):
        path = str(tmp_path / "existing_key.pem")
        original = KeyGenerator.load_or_create(path)
        loaded = KeyGenerator.load_or_create(path)
        assert loaded.private_numbers() == original.private_numbers()


class TestCreateRootCa:

    def test_returns_certificate(self, cert_gen, root_key):
        cert = cert_gen.create_root_ca(root_key, TEST_SUBJECT_NAME)
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

    def test_validity_not_before_uses_provided_now(self, cert_gen, root_key, now):
        cert = cert_gen.create_root_ca(root_key, TEST_SUBJECT_NAME, validity_days=365)
        assert cert.not_valid_before_utc == now

    def test_validity_not_after_applies_validity_days(self, cert_gen, root_key, now):
        cert = cert_gen.create_root_ca(root_key, TEST_SUBJECT_NAME, validity_days=365)
        assert cert.not_valid_after_utc == now + timedelta(days=365)


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
        self, cert_gen, leaf_key, root_key, root_cert, now
    ):
        cert = cert_gen.create_certificate(
            leaf_key.public_key(), root_key, TEST_LEAF_NAME, root_cert,
            validity_days=100,
            extensions=LEAF_EXTENSIONS
        )
        assert cert.not_valid_after_utc == now + timedelta(days=100)
