from argparse import Namespace
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
