from argparse import ArgumentParser, Namespace
from pathlib import Path
import pytest
from mock_credential import GenerateMockCredentialInputs
import logging
from typing import Generator

LOGGER = logging.getLogger(__name__)


@pytest.fixture
def empty_parser() -> Generator[ArgumentParser, None, None]:
    yield ArgumentParser(description="Generate a mock credential for the test app")


@pytest.fixture
def issuer_auth_input_tmp_dir(tmp_path: Path) -> Generator[Path, None, None]:
    fixture_dir = tmp_path / "valid_parser"
    fixture_dir.mkdir(parents=True)
    yield fixture_dir


@pytest.fixture
def valid_parser(
    issuer_auth_input_tmp_dir: Path, empty_parser: ArgumentParser
) -> Generator[Namespace, None, None]:
    empty_parser.add_argument(
        "--private-key",
        help="Path to device private key PEM",
        default=str(issuer_auth_input_tmp_dir) + "/test_private_key.pem",
    )
    empty_parser.add_argument(
        "--reader-auth-private-key",
        default=str(issuer_auth_input_tmp_dir) + "/test_reader_auth_private_key.pem",
    )
    empty_parser.add_argument(
        "--issuer-private-key",
        help="The private EC key for the root CA",
        default=str(issuer_auth_input_tmp_dir) + "/test_private_issuer_key.pem",
    )
    empty_parser.add_argument(
        "--issuer-intermediate-x509-certificate",
        help="Output path for the intermediate CA certificate (DER format, trust anchor)",
        default=str(issuer_auth_input_tmp_dir) + "/test_x509_certificate.der",
    )
    empty_parser.add_argument(
        "--reader-intermediate-x509-certificate",
        default="app/src/main/assets/test_reader_auth_x509_certificate.der"
    )
    empty_parser.add_argument(
        "--output",
        help="Output path for credential txt file",
        default=str(issuer_auth_input_tmp_dir) + "/mock_credential.txt",
    )
    empty_parser.add_argument(
        "--validity-days",
        type=int,
        default=365,
        help="Validity period in days (leaf capped at 457)",
    )
    empty_parser.add_argument(
        "--reader-valid-x509-leaf-certificate",
        default=str(issuer_auth_input_tmp_dir) + "/reader_valid_x509_leaf_certificate.der"
    )
    empty_parser.add_argument(
        "--reader-invalid-x509-leaf-certificate",
        default=str(issuer_auth_input_tmp_dir) + "/reader_invalid_x509_leaf_certificate.der"
    )
    args, unknown = empty_parser.parse_known_args()
    yield args


@pytest.fixture
def valid_issuer_auth_input(valid_parser) -> Generator[GenerateMockCredentialInputs, None, None]:
    yield GenerateMockCredentialInputs.from_parser(valid_parser)
