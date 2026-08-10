from argparse import Namespace
import pytest
from mock_credential.issuer import IssuerAuthInput


class TestIssuerAuthInput:

    def test_valid_parser_input(
        self, valid_parser: Namespace, valid_issuer_auth_input: IssuerAuthInput
    ):
        assert IssuerAuthInput.from_parser(valid_parser) == valid_issuer_auth_input

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
        ],
    )
    def test_invalid_parser_input(self, invalid_input: Namespace, missing_attribute: str):
        with pytest.raises(AttributeError) as exception:
            IssuerAuthInput.from_parser(invalid_input)

        assert f"'Namespace' object has no attribute '{missing_attribute}'" in str(exception.value)
