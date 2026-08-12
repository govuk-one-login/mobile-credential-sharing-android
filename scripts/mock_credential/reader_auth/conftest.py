from datetime import datetime
from pytest import fixture

from mock_credential.reader_auth import ReaderAuthCertificateGenerator

@fixture
def reader_auth_cert_gen():
    return ReaderAuthCertificateGenerator()
