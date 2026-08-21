import pytest
from argparse import Namespace
from mock_credential import GenerateMockCredentialInputs
from mock_credential.conftest import get_der_certificate_output


class TestIssuerAuthInput:

    def test_valid_parser_input(
        self, valid_parser: Namespace, valid_issuer_auth_input: GenerateMockCredentialInputs
    ):
        assert GenerateMockCredentialInputs.from_parser(valid_parser) == valid_issuer_auth_input

    @pytest.mark.parametrize(
        "invalid_input,missing_attribute",
        [
            pytest.param(Namespace(), "issuer_private_key", id="Missing issuer private key"),
            pytest.param(
                Namespace(issuer_private_key=""),
                "reader_auth_private_key",
                id="Missing reader auth private key",
            ),
            pytest.param(
                Namespace(issuer_private_key="", reader_auth_private_key=""),
                "output",
                id="Missing output",
            ),
            pytest.param(
                Namespace(issuer_private_key="", reader_auth_private_key="", output=""),
                "private_key",
                id="Missing private key",
            ),
            pytest.param(
                Namespace(
                    issuer_private_key="", reader_auth_private_key="", output="", private_key=""
                ),
                "validity_days",
                id="Missing validity days",
            ),
            pytest.param(
                Namespace(
                    issuer_private_key="",
                    reader_auth_private_key="",
                    output="",
                    private_key="",
                    validity_days=1,
                ),
                "issuer_intermediate_x509_certificate",
                id="Missing Issuer x509 intermediary certificate",
            ),
            pytest.param(
                Namespace(
                    issuer_private_key="",
                    reader_auth_private_key="",
                    output="",
                    private_key="",
                    validity_days=1,
                    issuer_intermediate_x509_certificate="",
                ),
                "reader_intermediate_x509_certificate",
                id="Missing ReaderAuth x509 intermediary certificate",
            ),
            pytest.param(
                Namespace(
                    issuer_private_key="",
                    reader_auth_private_key="",
                    output="",
                    private_key="",
                    validity_days=1,
                    issuer_intermediate_x509_certificate="",
                    reader_intermediate_x509_certificate="",
                ),
                "reader_valid_x509_leaf_certificate",
                id="Missing valid ReaderAuth x509 leaf certificate",
            ),
            pytest.param(
                Namespace(
                    issuer_private_key="",
                    reader_auth_private_key="",
                    output="",
                    private_key="",
                    validity_days=1,
                    issuer_intermediate_x509_certificate="",
                    reader_intermediate_x509_certificate="",
                    reader_valid_x509_leaf_certificate="",
                ),
                "reader_x509_leaf_certificate_without_privacy_policy",
                id="ReaderAuth x509 leaf certificate without privacy policy",
            ),
            pytest.param(
                Namespace(
                    issuer_private_key="",
                    reader_auth_private_key="",
                    output="",
                    private_key="",
                    validity_days=1,
                    issuer_intermediate_x509_certificate="",
                    reader_intermediate_x509_certificate="",
                    reader_x509_leaf_certificate_without_privacy_policy="",
                    reader_valid_x509_leaf_certificate="",
                ),
                "reader_name_constrained_intermediate_x509_certificate",
                id="Missing ReaderAuth x509 intermediate certificate with NameConstraints",
            ),
        ],
    )
    def test_invalid_parser_input(self, invalid_input: Namespace, missing_attribute: str):
        with pytest.raises(AttributeError) as exception:
            GenerateMockCredentialInputs.from_parser(invalid_input)

        assert f"'Namespace' object has no attribute '{missing_attribute}'" in str(exception.value)

    def test_evaluated_reader_auth_certs_reference_version(
        self, reader_auth_leaf_certificate_contents: str
    ):
        assert "Version: 3 (0x2)" in reader_auth_leaf_certificate_contents

    def test_evaluated_reader_auth_certs_reference_signature_algorithm(
        self, reader_auth_leaf_certificate_contents: str
    ):
        assert "Signature Algorithm: ecdsa-with-SHA256" in reader_auth_leaf_certificate_contents

    def test_evaluated_reader_auth_certs_reference_public_key_algorithm(
        self, reader_auth_leaf_certificate_contents: str
    ):
        assert "Public Key Algorithm: id-ecPublicKey" in reader_auth_leaf_certificate_contents

    def test_evaluated_reader_auth_certs_reference_public_key_size(
        self, reader_auth_leaf_certificate_contents: str
    ):
        assert "Public-Key: (256 bit)" in reader_auth_leaf_certificate_contents

    def test_evaluated_reader_auth_certs_reference_asn_oid(
        self, reader_auth_leaf_certificate_contents: str
    ):
        assert "ASN1 OID: prime256v1" in reader_auth_leaf_certificate_contents

    def test_evaluated_reader_auth_certs_reference_curve(
        self, reader_auth_leaf_certificate_contents: str
    ):
        assert "NIST CURVE: P-256" in reader_auth_leaf_certificate_contents

    def test_valid_reader_auth_cert_references_privacy_policy_oid(
        self, valid_reader_auth_leaf_path: str
    ):
        contents = get_der_certificate_output(valid_reader_auth_leaf_path)
        assert "1.3.6.1.4.1.66559.1.1 - URI:https://www.gov.uk/" in contents

    def test_invalid_reader_auth_cert_does_not_reference_privacy_policy_oid(
        self, invalid_reader_auth_leaf_path: str
    ):
        contents = get_der_certificate_output(invalid_reader_auth_leaf_path)
        assert "1.3.6.1.4.1.66559.1.1 - URI:https://www.gov.uk/" not in contents
