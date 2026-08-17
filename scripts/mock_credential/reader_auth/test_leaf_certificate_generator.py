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
    Name,
    Version,
    SignatureAlgorithmOID,
    PublicKeyAlgorithmOID,
    SubjectInformationAccess,
    SubjectKeyIdentifier,
    ObjectIdentifier,
    UniformResourceIdentifier,
)
from cryptography.x509.oid import AuthorityInformationAccessOID
from cryptography.hazmat.primitives.asymmetric.ec import EllipticCurvePrivateKey
from datetime import datetime, timezone

from mock_credential.reader_auth import OID_MDL_RA, OID_MDOC_RA, READER_AUTH_LEAF_SUBJECT_NAME


class TestReaderAuthLeafCertificateGenerator:
    def test_version_is_3(self, leaf_certificates: Certificate):
        assert leaf_certificates.version == Version.v3

    def test_signature_algorithm(self, leaf_certificates: Certificate):
        assert leaf_certificates.signature_algorithm_oid == SignatureAlgorithmOID.ECDSA_WITH_SHA256

    def test_issuer_matches_subject(
        self,
        valid_intermediate_cert: Certificate,
        subject_name: Name,
        leaf_certificates: Certificate,
    ):
        assert leaf_certificates.subject == subject_name
        assert leaf_certificates.issuer == valid_intermediate_cert.subject

    def test_validity_not_before_matches_generator_property(
        self, leaf_certificates: Certificate, reader_auth_now
    ):
        expected = datetime(
            year=reader_auth_now.year,
            month=reader_auth_now.month,
            day=reader_auth_now.day,
            hour=reader_auth_now.hour,
            minute=reader_auth_now.minute,
            second=reader_auth_now.second,
        )
        assert leaf_certificates.not_valid_before == expected
        assert leaf_certificates.not_valid_before_utc == expected.replace(tzinfo=timezone.utc)

    def test_validity_not_after_defaults_to_one_year(
        self, leaf_certificates: Certificate, reader_auth_now
    ):
        expected = datetime(
            year=reader_auth_now.year + 1,
            month=reader_auth_now.month,
            day=reader_auth_now.day,
            hour=reader_auth_now.hour,
            minute=reader_auth_now.minute,
            second=reader_auth_now.second,
        )
        assert leaf_certificates.not_valid_after == expected

    def test_leaf_certificate_has_common_name(self, leaf_certificates: Certificate):
        assert READER_AUTH_LEAF_SUBJECT_NAME == leaf_certificates.subject

    def test_public_key_algorithm_is_id_ecPublicKey(self, leaf_certificates: Certificate):
        assert leaf_certificates.public_key_algorithm_oid == PublicKeyAlgorithmOID.EC_PUBLIC_KEY

    def test_basic_constraints_is_critical(self, leaf_certificate_extensions: Extensions):
        bc = leaf_certificate_extensions.get_extension_for_class(BasicConstraints)
        assert bc.critical

    def test_authority_identifier_derived_from_root_public_key(
        self, root_key: EllipticCurvePrivateKey, leaf_certificate_extensions: Extensions
    ):
        authority = leaf_certificate_extensions.get_extension_for_class(AuthorityKeyIdentifier)

        assert (
            AuthorityKeyIdentifier.from_issuer_public_key(root_key.public_key()).key_identifier
            == authority.value.key_identifier
        )

    @pytest.mark.parametrize(
        "leaf_key_fixture,certificate_fixture",
        [
            pytest.param("valid_leaf_key", "valid_leaf_certificate", id="valid_leaf_certificate"),
            pytest.param(
                "invalid_leaf_key", "invalid_leaf_certificate", id="invalid_leaf_certificate"
            ),
        ],
    )
    def test_subject_key_identifier_derived_from_leaf_public_key(
        self, leaf_key_fixture: str, certificate_fixture: str, request
    ):
        leaf_key = request.getfixturevalue(leaf_key_fixture)
        certificate = request.getfixturevalue(certificate_fixture)
        subject = certificate.extensions.get_extension_for_class(SubjectKeyIdentifier)

        assert (
            SubjectKeyIdentifier.from_public_key(leaf_key.public_key()).key_identifier
            == subject.value.key_identifier
        )

    def test_key_usage_is_critical(self, leaf_key_usage: Extension[KeyUsage]):
        assert leaf_key_usage.critical

    def test_key_usage_mandatory_fields(self, leaf_key_usage: Extension[KeyUsage]):
        assert leaf_key_usage.value.digital_signature

    def test_key_usage_false_key_encipherment(
        self,
        leaf_key_usage: Extension[KeyUsage],
    ):
        assert not leaf_key_usage.value.key_encipherment

    def test_key_usage_false_data_encipherment(
        self,
        leaf_key_usage: Extension[KeyUsage],
    ):
        assert not leaf_key_usage.value.data_encipherment

    def test_key_usage_false_key_agreement(
        self,
        leaf_key_usage: Extension[KeyUsage],
    ):
        assert not leaf_key_usage.value.key_agreement

    def test_key_usage_false_key_certificate_signature(
        self,
        leaf_key_usage: Extension[KeyUsage],
    ):
        assert not leaf_key_usage.value.key_cert_sign

    def test_key_usage_false_key_crl_signature(
        self,
        leaf_key_usage: Extension[KeyUsage],
    ):
        assert not leaf_key_usage.value.crl_sign

    def test_key_usage_cannot_configure_encipher_only(
        self,
        leaf_key_usage: Extension[KeyUsage],
    ):
        with raises(ValueError) as exception:
            leaf_key_usage.value.encipher_only

        assert "encipher_only is undefined unless key_agreement is true" in str(exception)

    def test_key_usage_cannot_configure_decipher_only(
        self,
        leaf_key_usage: Extension[KeyUsage],
    ):
        with raises(ValueError) as exception:
            leaf_key_usage.value.decipher_only

        assert "decipher_only is undefined unless key_agreement is true" in str(exception)

    def test_extended_key_usage_is_critical(self, leaf_certificate_extensions: Extensions):
        extended_keys = leaf_certificate_extensions.get_extension_for_class(ExtendedKeyUsage)

        assert extended_keys.critical

    @pytest.mark.parametrize(
        "input",
        [
            pytest.param(OID_MDL_RA, id="ReaderAuth: Mobile Driving Licence"),
            pytest.param(OID_MDOC_RA, id="ReaderAuth: Mobile Document"),
        ],
    )
    def test_extended_keys_contain_input(
        self, input: ObjectIdentifier, leaf_certificate_extensions: Extensions
    ):
        extended_keys = leaf_certificate_extensions.get_extension_for_class(ExtendedKeyUsage)

        assert input in extended_keys.value

    def test_has_ocsp_access_description(self, leaf_certificate_extensions: Extensions):
        access_method = leaf_certificate_extensions.get_extension_for_class(
            AuthorityInformationAccess
        )

        assert (
            AccessDescription(
                access_method=AuthorityInformationAccessOID.OCSP,
                access_location=UniformResourceIdentifier("https://www.gov.uk/"),
            )
            in access_method.value
        )

    def test_signature_is_ecdsa_sha_256(self, leaf_certificates: Certificate):
        assert leaf_certificates.signature_algorithm_oid == SignatureAlgorithmOID.ECDSA_WITH_SHA256

    def test_invalid_leaf_certificate_has_no_subject_information_access(
        self, invalid_leaf_certificate: Certificate
    ):
        with raises(ExtensionNotFound) as exception:
            invalid_leaf_certificate.extensions.get_extension_for_class(SubjectInformationAccess)

        assert (
            "No <class 'cryptography.x509.extensions.SubjectInformationAccess'> extension was found"
            in str(exception)
        )

    def test_valid_leaf_certificate_has_privacy_policy_url(
        self, valid_leaf_certificate: Certificate
    ):
        subject_info_access = valid_leaf_certificate.extensions.get_extension_for_class(
            SubjectInformationAccess
        )

        assert (
            AccessDescription(
                access_method=ObjectIdentifier("1.3.6.1.4.1.72548.1.1"),
                access_location=UniformResourceIdentifier("https://www.gov.uk/"),
            )
            in subject_info_access.value
        )
