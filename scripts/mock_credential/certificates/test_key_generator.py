from pytest import fixture
from cryptography.hazmat.primitives.asymmetric.ec import EllipticCurvePrivateKey, EllipticCurve, SECP256R1
from mock_credential.certificates import KeyGenerator

class TestKeyGenerator():

    @fixture
    def generated_key(self) -> EllipticCurvePrivateKey:
        return KeyGenerator().generate()

    def test_generated_keys_use_p256_curve(
        self,
        generated_key: EllipticCurvePrivateKey
    ):
        assert isinstance(generated_key.curve, SECP256R1)