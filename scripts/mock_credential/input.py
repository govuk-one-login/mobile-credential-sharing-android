from argparse import Namespace
import logging
from logging518 import config as logging_config
from typing import List, TypeVar, Type, Tuple

from cryptography.hazmat.primitives.asymmetric.ec import EllipticCurvePrivateKey
from cryptography.hazmat.primitives.asymmetric.types import PrivateKeyTypes
from cryptography.hazmat.primitives import serialization
from cryptography.x509 import Certificate, ExtendedKeyUsage, KeyUsage, IssuerAlternativeName, UniformResourceIdentifier, ExtensionType
from cryptography.x509.oid import ObjectIdentifier

from mock_credential.issuer_auth import ISSUER_NAME, LEAF_NAME
from mock_credential.reader_auth import (
    READER_AUTH_COMMON_LEAF_EXTENSIONS,
    READER_AUTH_LEAF_SUBJECT_NAME,
    PRIVACY_POLICY_URL_EXTENSION
)
from mock_credential.certificates.generators import (
    CertificateGenerator,
    KeyGenerator,
)

T = TypeVar("T", bound="Parent")  # type: ignore
# ISO 18013-5 mdoc DS OID
OID_MDL_DS = ObjectIdentifier("1.0.18013.5.1.2")
# mdlReaderAuth
OID_MDL_RA = ObjectIdentifier("1.0.18013.5.1.6")
# mdocReaderAuth
OID_MDOC_RA = ObjectIdentifier("1.0.23220.4.1.6")

logging_config.fileConfig("pyproject.toml")
logger = logging.getLogger("project")
issuer_logger = logging.getLogger("IssuerAuth")
reader_logger = logging.getLogger("ReaderAuth")

class GenerateMockCredentialInputs:

    def __init__(self, **kwargs):
        self.issuer_private_key = kwargs["issuer_private_key"]
        self.reader_auth_private_key = kwargs["reader_auth_private_key"]
        self.output = kwargs["output"]
        self.private_key = kwargs["private_key"]
        self.validity_days = kwargs["validity_days"]
        self.issuer_intermediate_x509_certificate = kwargs["issuer_intermediate_x509_certificate"]
        self.reader_intermediate_x509_certificate = kwargs["reader_intermediate_x509_certificate"]
        self.reader_valid_x509_leaf_certificate = kwargs["reader_valid_x509_leaf_certificate"]
        self.reader_invalid_x509_leaf_certificate = kwargs["reader_invalid_x509_leaf_certificate"]

        logger.info("Created input instance from parameters")


    def __eq__(self, other):
        return (
            isinstance(other, GenerateMockCredentialInputs)
            and (self.issuer_private_key == other.issuer_private_key)
            and (self.reader_auth_private_key == other.reader_auth_private_key)
            and (self.output == other.output)
            and (self.private_key == other.private_key)
            and (self.validity_days == other.validity_days)
            and (self.issuer_intermediate_x509_certificate == other.issuer_intermediate_x509_certificate)
            and (self.reader_valid_x509_leaf_certificate == other.reader_valid_x509_leaf_certificate)
            and (self.reader_invalid_x509_leaf_certificate == other.reader_invalid_x509_leaf_certificate)
        )

    def create_issuer_auth_intermediate_certificate(
        self,
        cert_gen: CertificateGenerator,
        key_gen: KeyGenerator
    ) -> Tuple[PrivateKeyTypes, Certificate]:
        issuer_private_key = key_gen.load_or_create(self.issuer_private_key)
        intermediary_issuer_auth_cert = cert_gen.create_intermediate(
            private_key=issuer_private_key,
            subject=ISSUER_NAME,
            validity_days=self.validity_days,
        )
        with open(self.issuer_intermediate_x509_certificate, "wb") as f:
            f.write(intermediary_issuer_auth_cert.public_bytes(serialization.Encoding.DER))
            issuer_logger.info(f"Written x509 intermediate certificate: {self.issuer_intermediate_x509_certificate}")

        return (issuer_private_key, intermediary_issuer_auth_cert)


    def create_issuer_auth_certificates(
        self,
        cert_gen: CertificateGenerator,
        key_gen: KeyGenerator,
    ) -> Tuple[EllipticCurvePrivateKey, Certificate]:
        issuer_private_key, intermediary_issuer_auth_cert = self.create_issuer_auth_intermediate_certificate(
            cert_gen=cert_gen,
            key_gen=key_gen
        )
        
        issuer_leaf_key = key_gen.generate()
        issuer_logger.info("Created leaf certificate key")
        issuer_leaf_cert = cert_gen.create_certificate(
            issuer_leaf_key.public_key(),
            issuer_private_key,
            LEAF_NAME,
            intermediary_issuer_auth_cert,
            validity_days=min(self.validity_days, 457),
            extensions=[
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
                (ExtendedKeyUsage([OID_MDL_DS]), True),
                (
                    IssuerAlternativeName(
                        [UniformResourceIdentifier("https://dvla.gov.uk/iaca")]
                    ),
                    False,
                ),
            ],
        )

        return (issuer_leaf_key, issuer_leaf_cert)


    def create_reader_auth_certificates(
        self,
        cert_gen: CertificateGenerator,
        root_key_gen: KeyGenerator,
        intermediate_key_gen: KeyGenerator,
    ) -> Tuple[Certificate, Certificate]:
        reader_auth_private_key = root_key_gen.load_or_create(self.reader_auth_private_key)
        intermediary_reader_auth_cert = cert_gen.create_intermediate(
            private_key=reader_auth_private_key,
            subject=ISSUER_NAME,
            validity_days=self.validity_days,
        )
        with open(self.reader_intermediate_x509_certificate, "wb") as f:
            f.write(intermediary_reader_auth_cert.public_bytes(serialization.Encoding.DER))
            reader_logger.info(f"Written x509 intermediate certificate: {self.reader_intermediate_x509_certificate}")

        (
            _,
            valid_reader_auth_leaf_cert
        ) = self._create_reader_auth_leaf_certificate(
            cert_gen=cert_gen,
            key_gen=intermediate_key_gen,
            reader_auth_private_key=reader_auth_private_key,
            intermediary_reader_auth_cert=intermediary_reader_auth_cert,
            extensions=READER_AUTH_COMMON_LEAF_EXTENSIONS + [
                (
                    PRIVACY_POLICY_URL_EXTENSION,
                    False
                )
            ]
        )
        with open(self.reader_valid_x509_leaf_certificate, "wb") as f:
            f.write(valid_reader_auth_leaf_cert.public_bytes(serialization.Encoding.DER))
            reader_logger.info(
                f"Written valid x509 leaf certificate: {self.reader_valid_x509_leaf_certificate}"
            )

        (
            _,
            invalid_reader_auth_leaf_cert
        ) = self._create_reader_auth_leaf_certificate(
            cert_gen=cert_gen,
            key_gen=intermediate_key_gen,
            reader_auth_private_key=reader_auth_private_key,
            intermediary_reader_auth_cert=intermediary_reader_auth_cert,
            # Doesn't contain Privacy policy OID
            extensions=READER_AUTH_COMMON_LEAF_EXTENSIONS
        )
        with open(self.reader_invalid_x509_leaf_certificate, "wb") as f:
            f.write(invalid_reader_auth_leaf_cert.public_bytes(serialization.Encoding.DER))
            reader_logger.info(
                f"Written invalid x509 leaf certificate: {self.reader_invalid_x509_leaf_certificate}"
            )


        return (valid_reader_auth_leaf_cert, invalid_reader_auth_leaf_cert)


    def _create_reader_auth_leaf_certificate(
        self,
        cert_gen: CertificateGenerator,
        reader_auth_private_key: PrivateKeyTypes,
        intermediary_reader_auth_cert: Certificate,
        extensions: List[Tuple[ExtensionType, bool]],
        key_gen: KeyGenerator
    ) -> Tuple[EllipticCurvePrivateKey, Certificate]:
        
        reader_auth_leaf_key = key_gen.generate()
        reader_logger.info("Created leaf certificate key")
        reader_auth_leaf_cert = cert_gen.create_certificate(
            reader_auth_leaf_key.public_key(),
            reader_auth_private_key,
            READER_AUTH_LEAF_SUBJECT_NAME,
            intermediary_reader_auth_cert,
            validity_days=min(self.validity_days, 457),
            extensions=extensions,
        )

        return (reader_auth_leaf_key, reader_auth_leaf_cert)


    @classmethod
    def from_parser(cls: Type[T], args: Namespace) -> T:
        """
        Create an instance of :class:`IssuerAuthInput` from the provided argument parser.

        Expected arguments from :param:`args`:
        - "private_key"
        - "x509_certificate"
        - "issuer_private_key"
        - "output"
        - "validity_days"

        :param cls: The class :class:`Type`. This defers to :class:`IssuerAuthInput`.
        :param args: The parsed arguments from :class:`ArgumentParser`. Generate this via
            :func:`ArgumentParser.parse_args`.
        :return: An instance of :class:`IssuerAuthInput` with the necessary data.
        """

        return cls(
            issuer_private_key=args.issuer_private_key,
            reader_auth_private_key=args.reader_auth_private_key,
            output=args.output,
            private_key=args.private_key,
            validity_days=args.validity_days,
            issuer_intermediate_x509_certificate=args.issuer_intermediate_x509_certificate,
            reader_intermediate_x509_certificate=args.reader_intermediate_x509_certificate,
            reader_valid_x509_leaf_certificate=args.reader_valid_x509_leaf_certificate,
            reader_invalid_x509_leaf_certificate=args.reader_invalid_x509_leaf_certificate,
        )
