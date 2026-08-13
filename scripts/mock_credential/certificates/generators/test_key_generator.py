from pytest import fixture, raises
from cryptography.hazmat.primitives.asymmetric.ec import EllipticCurvePrivateKey, EllipticCurve, SECP256R1
from mock_credential.certificates import KeyGenerator

class TestKeyGenerator():
    @fixture
    def generated_key(self, key_gen: KeyGenerator) -> EllipticCurvePrivateKey:
        return key_gen.generate()

    def test_generated_keys_use_p256_curve(self, generated_key: EllipticCurvePrivateKey):
        assert isinstance(generated_key.curve, SECP256R1)
    
    def test_returns_ec_private_key(self, generated_key: EllipticCurvePrivateKey):
        assert isinstance(generated_key, EllipticCurvePrivateKey)

    def test_each_call_produces_unique_key(self, generated_key: EllipticCurvePrivateKey):
        assert generated_key.private_numbers() != KeyGenerator().generate().private_numbers()

    
    def test_load_returns_private_key(
        self,
        tmp_path,
        generated_key: EllipticCurvePrivateKey,
        key_gen: KeyGenerator
    ):
        path = str(tmp_path / "key.pem")
        with open(path, "xb") as f:
            from cryptography.hazmat.primitives import serialization

            f.write(
                generated_key.private_bytes(
                    encoding=serialization.Encoding.PEM,
                    format=serialization.PrivateFormat.PKCS8,
                    encryption_algorithm=serialization.NoEncryption(),
                )
            )
        loaded = key_gen.load_pem(path)
        assert isinstance(loaded, EllipticCurvePrivateKey)

    def test_load_returns_same_key(
        self,
        tmp_path,
        generated_key: EllipticCurvePrivateKey,
        key_gen: KeyGenerator
    ):
        path = str(tmp_path / "key.pem")
        with open(path, "xb") as f:
            from cryptography.hazmat.primitives import serialization

            f.write(
                generated_key.private_bytes(
                    encoding=serialization.Encoding.PEM,
                    format=serialization.PrivateFormat.PKCS8,
                    encryption_algorithm=serialization.NoEncryption(),
                )
            )
        loaded = key_gen.load_pem(path)
        assert loaded.private_numbers() == generated_key.private_numbers()

    def test_load_raises_when_file_missing(self, tmp_path, key_gen: KeyGenerator):
        with raises(FileNotFoundError):
            key_gen.load_pem(str(tmp_path / "missing.pem"))
    
    def test_creates_key_when_file_missing(self, tmp_path, key_gen: KeyGenerator):
        path = str(tmp_path / "new_key.pem")
        key = key_gen.load_or_create_pem(path)
        assert isinstance(key, EllipticCurvePrivateKey)

    def test_saves_key_to_disk_when_created(self, tmp_path, key_gen: KeyGenerator):
        path = str(tmp_path / "new_key.pem")
        key_gen.load_or_create_pem(path)
        assert (tmp_path / "new_key.pem").exists()

    def test_loads_existing_key_when_file_present(self, tmp_path, key_gen: KeyGenerator):
        path = str(tmp_path / "existing_key.pem")
        original = key_gen.load_or_create_pem(path)
        loaded = key_gen.load_or_create_pem(path)
        assert loaded.private_numbers() == original.private_numbers()