from pytest import raises
import pytest
from cryptography.x509 import (
    AccessDescription,
    AuthorityInformationAccess,
    Certificate,
    Extension,
    ExtensionNotFound,
    Extensions,
    AuthorityKeyIdentifier,
    BasicConstraints,
    KeyUsage,
    ExtendedKeyUsage,
    Version,
    SignatureAlgorithmOID,
    NameOID,
    NameAttribute,
    PublicKeyAlgorithmOID,
    SubjectInformationAccess,
    SubjectKeyIdentifier,
    ObjectIdentifier,
    UniformResourceIdentifier,
)
from cryptography.x509.oid import AuthorityInformationAccessOID
from cryptography.hazmat.primitives.asymmetric.ec import EllipticCurvePrivateKey
from datetime import datetime, timezone

from mock_credential.certificates.generators.conftest import TEST_SUBJECT_NAME
from mock_credential.reader_auth import (
    OID_MDL_RA,
    OID_MDOC_RA,
)


class TestReaderAuthIntermediateCertificateGenerator:

    def test_version_is_3(self, valid_intermediate_cert: Certificate):
        assert valid_intermediate_cert.version == Version.v3

    def test_issuer_matches_subject(self, valid_intermediate_cert: Certificate):
        assert valid_intermediate_cert.subject == TEST_SUBJECT_NAME
        assert valid_intermediate_cert.issuer == valid_intermediate_cert.subject

    def test_validity_not_before_matches_generator_property(
        self, valid_intermediate_cert: Certificate, reader_auth_now
    ):
        expected = datetime(
            year=reader_auth_now.year,
            month=reader_auth_now.month,
            day=reader_auth_now.day,
            hour=reader_auth_now.hour,
            minute=reader_auth_now.minute,
            second=reader_auth_now.second,
        )
        assert valid_intermediate_cert.not_valid_before == expected
        assert valid_intermediate_cert.not_valid_before_utc == expected.replace(tzinfo=timezone.utc)

    def test_validity_not_after_defaults_to_one_year(
        self, valid_intermediate_cert: Certificate, reader_auth_now
    ):
        expected = datetime(
            year=reader_auth_now.year + 1,
            month=reader_auth_now.month,
            day=reader_auth_now.day,
            hour=reader_auth_now.hour,
            minute=reader_auth_now.minute,
            second=reader_auth_now.second,
        )
        assert valid_intermediate_cert.not_valid_after == expected

    def test_certificate_has_common_name(self, valid_intermediate_cert: Certificate):
        assert NameAttribute(NameOID.COMMON_NAME, "Test Issuer") in valid_intermediate_cert.subject

    def test_public_key_algorithm_is_id_ecPublicKey(self, valid_intermediate_cert: Certificate):
        assert (
            valid_intermediate_cert.public_key_algorithm_oid == PublicKeyAlgorithmOID.EC_PUBLIC_KEY
        )

    def test_basic_constraints_is_critical(self, valid_intermediate_extensions: Extensions):
        bc = valid_intermediate_extensions.get_extension_for_class(BasicConstraints)
        assert bc.critical

    def test_authority_identifier_derived_from_public_key(
        self, root_key: EllipticCurvePrivateKey, valid_intermediate_extensions: Extensions
    ):
        authority = valid_intermediate_extensions.get_extension_for_class(AuthorityKeyIdentifier)

        assert (
            AuthorityKeyIdentifier.from_issuer_public_key(root_key.public_key()).key_identifier
            == authority.value.key_identifier
        )

    def test_subject_key_identifier_derived_from_public_key(
        self, root_key: EllipticCurvePrivateKey, valid_intermediate_extensions: Extensions
    ):
        subject = valid_intermediate_extensions.get_extension_for_class(SubjectKeyIdentifier)

        assert (
            SubjectKeyIdentifier.from_public_key(root_key.public_key()).key_identifier
            == subject.value.key_identifier
        )

    def test_key_usage_is_critical(self, valid_intermediate_key_usage: Extension[KeyUsage]):
        assert valid_intermediate_key_usage.critical

    def test_key_usage_mandatory_fields(self, valid_intermediate_key_usage: Extension[KeyUsage]):
        assert valid_intermediate_key_usage.value.digital_signature

    @staticmethod
    def _valid_key_encipherment(valid_intermediate_key_usage: Extension[KeyUsage]):
        return valid_intermediate_key_usage.value.key_encipherment

    def test_key_usage_false_key_encipherment(
        self,
        valid_intermediate_key_usage: Extension[KeyUsage],
    ):
        assert not valid_intermediate_key_usage.value.key_encipherment

    def test_key_usage_false_data_encipherment(
        self,
        valid_intermediate_key_usage: Extension[KeyUsage],
    ):
        assert not valid_intermediate_key_usage.value.data_encipherment

    def test_key_usage_false_key_agreement(
        self,
        valid_intermediate_key_usage: Extension[KeyUsage],
    ):
        assert not valid_intermediate_key_usage.value.key_agreement

    def test_key_usage_false_key_certificate_signature(
        self,
        valid_intermediate_key_usage: Extension[KeyUsage],
    ):
        assert not valid_intermediate_key_usage.value.key_cert_sign

    def test_key_usage_false_key_crl_signature(
        self,
        valid_intermediate_key_usage: Extension[KeyUsage],
    ):
        assert not valid_intermediate_key_usage.value.crl_sign

    def test_key_usage_cannot_configure_encipher_only(
        self,
        valid_intermediate_key_usage: Extension[KeyUsage],
    ):
        with raises(ValueError) as exception:
            valid_intermediate_key_usage.value.encipher_only

        assert "encipher_only is undefined unless key_agreement is true" in str(exception)

    def test_key_usage_cannot_configure_decipher_only(
        self,
        valid_intermediate_key_usage: Extension[KeyUsage],
    ):
        with raises(ValueError) as exception:
            valid_intermediate_key_usage.value.decipher_only

        assert "decipher_only is undefined unless key_agreement is true" in str(exception)

    def test_extended_key_usage_is_critical(self, valid_intermediate_extensions: Extensions):
        extended_keys = valid_intermediate_extensions.get_extension_for_class(ExtendedKeyUsage)

        assert extended_keys.critical

    @pytest.mark.parametrize(
        "input",
        [
            pytest.param(OID_MDL_RA, id="ReaderAuth: Mobile Driving Licence"),
            pytest.param(OID_MDOC_RA, id="ReaderAuth: Mobile Document"),
        ],
    )
    def test_extended_keys_contain_input(
        self, input: ObjectIdentifier, valid_intermediate_extensions: Extensions
    ):
        extended_keys = valid_intermediate_extensions.get_extension_for_class(ExtendedKeyUsage)

        assert input in extended_keys.value

    def test_intermediate_certificate_has_no_subject_information(
        self, valid_intermediate_extensions: Extensions
    ):

        with raises(ExtensionNotFound) as exception:
            valid_intermediate_extensions.get_extension_for_class(SubjectInformationAccess)

        assert (
            "No <class 'cryptography.x509.extensions.SubjectInformationAccess'> extension was found"
            in str(exception)
        )

    def test_has_ocsp_access_description(self, valid_intermediate_extensions: Extensions):
        access_method = valid_intermediate_extensions.get_extension_for_class(
            AuthorityInformationAccess
        )

        assert (
            AccessDescription(
                access_method=AuthorityInformationAccessOID.OCSP,
                access_location=UniformResourceIdentifier("https://www.gov.uk/"),
            )
            in access_method.value
        )

    def test_signature_is_ecdsa_sha_256(self, valid_intermediate_cert):
        assert (
            valid_intermediate_cert.signature_algorithm_oid
            == SignatureAlgorithmOID.ECDSA_WITH_SHA256
        )
