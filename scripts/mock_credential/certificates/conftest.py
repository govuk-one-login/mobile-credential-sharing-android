import pytest
from cryptography import x509
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.x509 import Name, NameAttribute
from cryptography.x509.oid import NameOID, ObjectIdentifier
from datetime import datetime, timezone

@pytest.fixture
def now() -> datetime:
    return datetime(2025, 1, 1, tzinfo=timezone.utc)
