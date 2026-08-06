import cbor2
import base64
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives.asymmetric.utils import decode_dss_signature
from typing import Dict, List


class IssuerAuth:
    """Handles COSE_Sign1 signing for mdoc."""
    def __init__(self, mso_tagged_bytes: bytes, leaf_cert_der: bytes):
        self.mso_tagged_bytes = mso_tagged_bytes
        self.leaf_cert_der = leaf_cert_der
        self.protected = cbor2.dumps({1: -7})  # alg: ES256

    def sign(self, signing_key: ec.EllipticCurvePrivateKey) -> List:
        """Signs the MSO and returns the IssuerAuth COSE_Sign1 array."""
        sig_structure = cbor2.dumps(["Signature1", self.protected, b"", self.mso_tagged_bytes])
        signature_der = signing_key.sign(sig_structure, ec.ECDSA(hashes.SHA256()))

        r, s = decode_dss_signature(signature_der)
        signature = r.to_bytes(32, "big") + s.to_bytes(32, "big")

        return [
            self.protected,
            {33: self.leaf_cert_der},
            self.mso_tagged_bytes,
            signature
        ]


class Credential:
    """Final assembly of the mdoc credential."""
    def __init__(self, namespaces_dict: Dict, issuer_auth: List):
        self.namespaces_dict = namespaces_dict
        self.issuer_auth = issuer_auth

    def to_cbor(self) -> bytes:
        return cbor2.dumps({
            "nameSpaces": self.namespaces_dict,
            "issuerAuth": self.issuer_auth
        })

    def to_base64url(self) -> str:
        cred_bytes = self.to_cbor()
        return base64.urlsafe_b64encode(cred_bytes).rstrip(b"=").decode()
