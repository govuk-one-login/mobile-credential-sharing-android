from argparse import ArgumentParser, Namespace
from pathlib import Path
import pytest
from mock_credential.issuer import IssuerAuthInput
import logging
from typing import Generator

LOGGER = logging.getLogger(__name__)

@pytest.fixture
def empty_parser() -> Generator[ArgumentParser, None, None]:
    yield ArgumentParser(description="Generate a mock credential for the test app")

@pytest.fixture
def issuer_auth_input_tmp_dir(tmp_path: Path) -> Generator[Path, None, None]:
    fixture_dir = tmp_path / "valid_parser"
    fixture_dir.mkdir(parents = True) 
    yield fixture_dir

@pytest.fixture
def valid_parser(
    issuer_auth_input_tmp_dir: Path,
    empty_parser: ArgumentParser
) -> Generator[Namespace, None, None]:
    empty_parser.add_argument(
        "--private-key",
        help="Path to device private key PEM",
        default=str(issuer_auth_input_tmp_dir) + "/test_private_key.pem"
    )
    empty_parser.add_argument(
        "--issuer-private-key",
        help="The private EC key for the root CA",
        default=str(issuer_auth_input_tmp_dir) + "/test_private_issuer_key.pem"
    )
    empty_parser.add_argument(
        "--x509-certificate",
        help="Output path for the root CA certificate (DER format, trust anchor)",
        default=str(issuer_auth_input_tmp_dir) + "/test_x509_certificate.der"
    )
    empty_parser.add_argument(
        "--output",
        help="Output path for credential txt file",
        default=str(issuer_auth_input_tmp_dir)  + "/mock_credential.txt"
    )
    empty_parser.add_argument(
        "--validity-days",
        type=int,
        default=365,
        help="Validity period in days (leaf capped at 457)"
    )
    args, unknown = empty_parser.parse_known_args()
    return args

@pytest.fixture
def valid_issuer_auth_input(valid_parser) -> Generator[IssuerAuthInput, None, None]:
    yield IssuerAuthInput.from_parser(valid_parser)
