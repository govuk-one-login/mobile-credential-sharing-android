import pytest
from datetime import datetime, timezone
from typing import Dict, Generator

from mock_credential.mso import MSO, SAMPLE_MSO
from mock_credential.namespaces import SAMPLE_NAMESPACES


@pytest.fixture
def device_cose_key() -> Dict:
    return {1: 2, -1: 1, -2: b"\x00" * 32, -3: b"\x00" * 32}


@pytest.fixture
def now() -> datetime:
    return datetime(2025, 1, 1, tzinfo=timezone.utc)


@pytest.fixture
def mso(device_cose_key: Dict) -> Generator[MSO, None, None]:
    yield MSO.from_dict(SAMPLE_MSO, device_cose_key)
