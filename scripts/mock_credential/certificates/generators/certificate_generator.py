from abc import ABCMeta, abstractmethod
from cryptography import x509
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.x509 import Certificate, Name
from typing import List


class CertificateGenerator(metaclass=ABCMeta):
    
    @abstractmethod
    def create_intermediate(
        self,
        private_key: ec.EllipticCurvePrivateKey,
        subject: Name,
        validity_days: int = 365,
    ) -> Certificate:
        """Generates a self-signed root CA certificate."""
        pass

    @abstractmethod
    def create_certificate(
        self,
        subject_key: ec.EllipticCurvePublicKey,
        issuer_key: ec.EllipticCurvePrivateKey,
        subject_name: Name,
        issuer_cert: Certificate,
        validity_days: int,
        extensions: List[tuple[x509.ExtensionType, bool]],
    ) -> Certificate:
        """Generic method to create a signed certificate (Intermediate or Leaf).

        Each entry in `extensions` is a (extension, critical) tuple, giving the
        caller explicit control over criticality.
        """
        pass


