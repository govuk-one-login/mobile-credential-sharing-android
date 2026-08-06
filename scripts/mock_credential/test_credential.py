import base64
import cbor2
import pytest
from cryptography.hazmat.primitives.asymmetric import ec

from mock_credential.certificates import IssuerAuth, Credential, KeyGenerator
from mock_credential.namespaces import SAMPLE_NAMESPACES


STUB_MSO_BYTES = cbor2.dumps(cbor2.CBORTag(24, cbor2.dumps({"version": "1.0"})))
STUB_CERT_DER = b"\x30\x82\x01\x00"  # minimal placeholder DER bytes


@pytest.fixture
def signing_key() -> ec.EllipticCurvePrivateKey:
    return KeyGenerator.generate()


@pytest.fixture
def issuer_auth(signing_key: ec.EllipticCurvePrivateKey) -> list:
    return IssuerAuth(STUB_MSO_BYTES, STUB_CERT_DER).sign(signing_key)


@pytest.fixture
def credential(issuer_auth: list) -> Credential:
    return Credential(SAMPLE_NAMESPACES.build_issuer_signed_items(), issuer_auth)


class TestIssuerAuth:

    def test_sign_returns_four_element_list(self, issuer_auth):
        assert len(issuer_auth) == 4

    def test_sign_protected_header_contains_es256(self, issuer_auth):
        protected = cbor2.loads(issuer_auth[0])
        assert protected == {1: -7}

    def test_sign_unprotected_header_contains_x5chain(self, issuer_auth):
        assert issuer_auth[1][33] == STUB_CERT_DER

    def test_sign_payload_is_mso_tagged_bytes(self, issuer_auth):
        assert issuer_auth[2] == STUB_MSO_BYTES

    def test_sign_signature_is_64_bytes(self, issuer_auth):
        assert len(issuer_auth[3]) == 64


class TestCredential:

    def test_to_cbor_returns_bytes(self, credential):
        assert isinstance(credential.to_cbor(), bytes)

    def test_to_cbor_contains_namespaces_key(self, credential):
        decoded = cbor2.loads(credential.to_cbor())
        assert "nameSpaces" in decoded

    def test_to_cbor_contains_issuer_auth_key(self, credential):
        decoded = cbor2.loads(credential.to_cbor())
        assert "issuerAuth" in decoded

    def test_to_base64url_returns_string(self, credential):
        assert isinstance(credential.to_base64url(), str)

    def test_to_base64url_has_no_padding(self, credential):
        assert "=" not in credential.to_base64url()

    def test_to_base64url_is_url_safe(self, credential):
        result = credential.to_base64url()
        assert "+" not in result
        assert "/" not in result

    def test_to_base64url_is_decodable(self, credential):
        result = credential.to_base64url()
        # Re-add padding for standard decode
        padded = result + "=" * (-len(result) % 4)
        decoded_bytes = base64.urlsafe_b64decode(padded)
        assert cbor2.loads(decoded_bytes) is not None
