from argparse import Namespace
import pytest
from mock_credential.issuer import IssuerAuthInput

class TestIssuerAuthInput:

    def test_valid_parser_input(
        self,
        valid_parser: Namespace,
        valid_issuer_auth_input: IssuerAuthInput
    ):
        assert IssuerAuthInput.from_parser(valid_parser) == valid_issuer_auth_input

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