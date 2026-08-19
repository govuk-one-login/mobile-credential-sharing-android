from cryptography import x509
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric.types import PublicKeyTypes, PrivateKeyTypes
from cryptography.x509 import (
    AuthorityInformationAccess,
    AccessDescription,
    UniformResourceIdentifier,
    BasicConstraints,
    KeyUsage,
    ExtendedKeyUsage,
    ObjectIdentifier,
    ExtensionType,
    Certificate,
    CertificateBuilder,
    Name,
    NameAttribute,
    NameOID,
    SubjectInformationAccess,
)
from cryptography.x509.oid import AuthorityInformationAccessOID
from datetime import datetime, timedelta, timezone
from mock_credential.certificates.generators import CertificateGenerator
from typing import List, Tuple

# mdlReaderAuth
OID_MDL_RA = ObjectIdentifier("1.0.18013.5.1.6")
# mdocReaderAuth
OID_MDOC_RA = ObjectIdentifier("1.0.23220.4.1.6")

READER_AUTH_LEAF_SUBJECT_NAME: Name = Name(
    [
        NameAttribute(NameOID.COUNTRY_NAME, "GB"),
        NameAttribute(NameOID.STATE_OR_PROVINCE_NAME, "London"),
        NameAttribute(NameOID.COMMON_NAME, "MegaDVS Intermediate"),
        NameAttribute(NameOID.ORGANIZATION_NAME, "MegaDVS"),
    ]
)

READER_AUTH_DVS_ATTRIBUTES: List[NameAttribute] = [
    NameAttribute(NameOID.ORGANIZATION_NAME, "MegaDVS"), 
]

def generate_dvs_subject(
    attributes: List[NameAttribute]
) -> Name:
    return Name(
        [NameAttribute(NameOID.COUNTRY_NAME, "GB")] + attributes
    )

READER_AUTH_COMMON_LEAF_EXTENSIONS: List[Tuple[ExtensionType, bool]] = [
    (
        KeyUsage(
            digital_signature=True,
            content_commitment=False,
            key_encipherment=False,
            data_encipherment=False,
            key_agreement=False,
            key_cert_sign=False,
            crl_sign=False,
            encipher_only=False,
            decipher_only=False,
        ),
        True,
    ),
    (ExtendedKeyUsage([OID_MDL_RA, OID_MDOC_RA]), True),
    (
        AuthorityInformationAccess(
            [
                AccessDescription(
                    access_method=AuthorityInformationAccessOID.OCSP,
                    access_location=UniformResourceIdentifier("https://www.gov.uk/"),
                )
            ]
        ),
        False,
    ),
]

PRIVACY_POLICY_URL_EXTENSION = SubjectInformationAccess(
    [
        AccessDescription(
            access_method=ObjectIdentifier(
                "1.3.6.1.4.1.66559.1.1",
            ),
            access_location=UniformResourceIdentifier("https://www.gov.uk/"),
        )
    ]
)


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
            .add_extension(x509.ExtendedKeyUsage([OID_MDL_RA, OID_MDOC_RA]), critical=True)
            .add_extension(
                x509.AuthorityInformationAccess(
                    [
                        x509.AccessDescription(
                            access_method=AuthorityInformationAccessOID.OCSP,
                            access_location=x509.UniformResourceIdentifier("https://www.gov.uk/"),
                        )
                    ]
                ),
                critical=False,
            )
        )
        return builder.sign(private_key, hashes.SHA256())

    def create_certificate(
        self,
        subject_key: PublicKeyTypes,
        issuer_key: PrivateKeyTypes,
        subject_name: Name,
        issuer_cert: Certificate,
        extensions: List[tuple[x509.ExtensionType, bool]],
        validity_days: int = 365,
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
