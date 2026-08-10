import pytest
from typing import Any, AnyStr, Dict
from mock_credential.namespaces import IssuerSignedItem

@pytest.fixture
def valid_issuer_signed_item_input() -> Dict[AnyStr, Any]:
    return {
        "digestID": 1,
        "elementIdentifier": "family_name",
        "elementValue": "Doe"
    }

@pytest.fixture
def valid_issuer_signed_item(valid_issuer_signed_item_input: Dict[AnyStr, Any]) -> IssuerSignedItem:
    return IssuerSignedItem(
        digest_id = valid_issuer_signed_item_input["digestID"],
        element_identifier = valid_issuer_signed_item_input["elementIdentifier"],
        element_value = valid_issuer_signed_item_input["elementValue"]
    )
