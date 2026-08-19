from subprocess import run, PIPE
from argparse import ArgumentParser, Namespace
from pathlib import Path
from pytest import fixture
import logging
from typing import Generator, Tuple
from cryptography.x509 import Certificate

from mock_credential import GenerateMockCredentialInputs
from mock_credential.certificates.generators import PemKeyGenerator, DerKeyGenerator
from mock_credential.reader_auth import ReaderAuthCertificateGenerator

LOGGER = logging.getLogger(__name__)


@fixture
def empty_parser() -> Generator[ArgumentParser, None, None]:
    yield ArgumentParser(description="Generate a mock credential for the test app")


@fixture
def input_tmp_dir(tmp_path: Path) -> Generator[Path, None, None]:
    fixture_dir = tmp_path / "input"
    fixture_dir.mkdir(parents=True)
    yield fixture_dir


@fixture
def valid_parser(
    input_tmp_dir: Path, empty_parser: ArgumentParser
) -> Generator[Namespace, None, None]:
    empty_parser.add_argument(
        "--private-key",
        help="Path to device private key PEM",
        default=str(input_tmp_dir) + "/test_private_key.pem",
    )
    empty_parser.add_argument(
        "--reader-auth-private-key",
        default=str(input_tmp_dir) + "/test_reader_auth_private_key.pem",
    )
    empty_parser.add_argument(
        "--issuer-private-key",
        help="The private EC key for the root CA",
        default=str(input_tmp_dir) + "/test_private_issuer_key.pem",
    )
    empty_parser.add_argument(
        "--issuer-intermediate-x509-certificate",
        help="Output path for the intermediate CA certificate (DER format, trust anchor)",
        default=str(input_tmp_dir) + "/test_x509_certificate.der",
    )
    empty_parser.add_argument(
        "--reader-intermediate-x509-certificate",
        default="app/src/main/assets/test_reader_auth_x509_certificate.der",
    )
    empty_parser.add_argument(
        "--output",
        help="Output path for credential txt file",
        default=str(input_tmp_dir) + "/mock_credential.txt",
    )
    empty_parser.add_argument(
        "--validity-days",
        type=int,
        default=365,
        help="Validity period in days (leaf capped at 457)",
    )
    empty_parser.add_argument(
        "--reader-valid-x509-leaf-certificate",
        default=str(input_tmp_dir) + "/reader_valid_x509_leaf_certificate.der",
    )
    empty_parser.add_argument(
        "--reader-x509-leaf-certificate-without-privacy-policy",
        default=str(input_tmp_dir) + "/reader_x509_leaf_without_privacy_policy.der",
    )
    empty_parser.add_argument(
        "--reader-name-constrained-intermediate-x509-certificate",
        default=str(input_tmp_dir) + "/reader_name_constrained_intermediate_x509_certificate.der",
    )
    empty_parser.add_argument(
        "--reader-x509-leaf-invalid-organisation",
        default=str(input_tmp_dir) + "/reader_x509_leaf_with_invalid_organisation.der",
    )
    args, _ = empty_parser.parse_known_args()
    yield args


@fixture
def valid_issuer_auth_input(valid_parser) -> Generator[GenerateMockCredentialInputs, None, None]:
    yield GenerateMockCredentialInputs.from_parser(valid_parser)


@fixture
def create_reader_auth_leaf_certificates(
    valid_issuer_auth_input: GenerateMockCredentialInputs,
) -> Tuple[Certificate, Certificate]:
    return valid_issuer_auth_input.create_reader_auth_certificates(
        cert_gen=ReaderAuthCertificateGenerator(),
        root_key_gen=PemKeyGenerator(),
        intermediate_key_gen=DerKeyGenerator(),
    )


@fixture
def valid_reader_auth_leaf_path(
    valid_issuer_auth_input: GenerateMockCredentialInputs,
    create_reader_auth_leaf_certificates: Tuple[Certificate, Certificate],
) -> str:
    return valid_issuer_auth_input.reader_valid_x509_leaf_certificate


@fixture
def invalid_reader_auth_leaf_path(
    valid_issuer_auth_input: GenerateMockCredentialInputs,
    create_reader_auth_leaf_certificates: Tuple[Certificate, Certificate],
) -> str:
    return valid_issuer_auth_input.reader_x509_leaf_certificate_without_privacy_policy


@fixture(
    params=[
        "invalid_reader_auth_leaf_path",
        "valid_reader_auth_leaf_path",
    ]
)
def reader_auth_leaf_paths(request) -> str:
    return request.getfixturevalue(request.param)


@fixture
def reader_auth_leaf_certificate_contents(reader_auth_leaf_paths: str) -> str:
    return get_der_certificate_output(reader_auth_leaf_paths)


def get_der_certificate_output(file_path: str) -> str:
    result = run(
        args=["openssl", "x509", "-in", file_path, "-inform", "DER", "-noout", "-text"], stdout=PIPE
    )

    return result.stdout.decode()
