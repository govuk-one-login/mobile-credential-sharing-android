from mock_credential.certificates.generators import CertificateGenerator


from cryptography import x509
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives.asymmetric.types import PublicKeyTypes, PrivateKeyTypes
from cryptography.x509 import Certificate, CertificateBuilder, Name


from datetime import datetime, timedelta, timezone
from typing import List


class ReaderAuthCertificateGenerator(CertificateGenerator):
    def __init__(self, now: datetime = datetime.now(tz=timezone.utc)):
        self.now = now

    def create_intermediate(
        self,
        private_key: PrivateKeyTypes,
        subject: Name,
        validity_days: int = 365,
    ) -> Certificate:
        """Generates a self-signed Intermediate certificate."""
        public_key = private_key.public_key()
        builder = (
            CertificateBuilder()
            .subject_name(subject)
            .issuer_name(subject)
            .public_key(public_key)
            .serial_number(x509.random_serial_number())
            .not_valid_before(self.now)
            .not_valid_after(self.now + timedelta(days=validity_days))
            .add_extension(x509.SubjectKeyIdentifier.from_public_key(public_key), critical=False)
            .add_extension(
                x509.AuthorityKeyIdentifier.from_issuer_public_key(public_key),
                critical=False,
            )
            .add_extension(x509.BasicConstraints(ca=True, path_length=None), critical=True)
            .add_extension(
                x509.KeyUsage(
                    key_cert_sign=False,
                    crl_sign=False,
                    digital_signature=True,
                    content_commitment=False,
                    key_encipherment=False,
                    data_encipherment=False,
                    key_agreement=False,
                    encipher_only=False,
                    decipher_only=False,
                ),
                critical=True,
            )
        )
        return builder.sign(private_key, hashes.SHA256())

    def create_certificate(
        self,
        subject_key: PublicKeyTypes,
        issuer_key: PrivateKeyTypes,
        subject_name: Name,
        issuer_cert: Certificate,
        validity_days: int,
        extensions: List[tuple[x509.ExtensionType, bool]],
    ) -> Certificate:
        """Generic method to create a signed certificate (Intermediate or Leaf).

        Each entry in `extensions` is a (extension, critical) tuple, giving the
        caller explicit control over criticality.
        """
        builder = (
            CertificateBuilder()
            .subject_name(subject_name)
            .issuer_name(issuer_cert.subject)
            .public_key(subject_key)
            .serial_number(x509.random_serial_number())
            .not_valid_before(self.now)
            .not_valid_after(self.now + timedelta(days=validity_days))
            .add_extension(x509.SubjectKeyIdentifier.from_public_key(subject_key), critical=False)
            .add_extension(
                x509.AuthorityKeyIdentifier.from_issuer_public_key(issuer_cert.public_key()),
                critical=False,
            )
        )

        for ext, critical in extensions:
            builder = builder.add_extension(ext, critical=critical)

        return builder.sign(issuer_key, hashes.SHA256())