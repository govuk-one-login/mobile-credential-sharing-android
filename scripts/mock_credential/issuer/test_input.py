from argparse import Namespace
import pytest
from mock_credential.issuer import IssuerAuthInput

class TestIssuerAuthInput:

    def test_valid_parser_input(
        self,
        valid_parser: Namespace,
        valid_issuer_auth_input: IssuerAuthInput
    ):
        assert valid_parser.private_key == valid_issuer_auth_input.private_key
        assert valid_parser.issuer_private_key == valid_issuer_auth_input.issuer_private_key
        assert valid_parser.x509_certificate == valid_issuer_auth_input.x509_certificate
        assert valid_parser.output == valid_issuer_auth_input.output
        assert valid_parser.validity_days == valid_issuer_auth_input.validity_days

    @pytest.mark.parametrize(
        "invalid_input",
        [
            Namespace(),
            Namespace(
                issuer_private_key = "",
            ),
            Namespace(
                issuer_private_key = "",
                output = "",
            ),
            Namespace(
                issuer_private_key = "",
                output = "",
                private_key = "",
            ),
            Namespace(
                issuer_private_key = "",
                output = "",
                private_key = "",
                validity_days = 1,
            )
        ],
        ids = [
            "Missing issuer private key",
            "Missing output",
            "Missing private key",
            "Missing validity days",
            "Missing x509 certificate",
        ]
    )
    def test_invalid_parser_input(self, invalid_input: Namespace):
        with pytest.raises(AttributeError):
            IssuerAuthInput.from_parser(invalid_input)