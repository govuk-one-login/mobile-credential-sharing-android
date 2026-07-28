import pytest
from . import IssuerSignedItem
from typing import AnyStr, Any, Dict

class TestIssuerSignedItem:

    def test_valid_input(
        self,
        valid_issuer_signed_item_input,
        valid_issuer_signed_item
    ):
        actual = IssuerSignedItem.from_dict(valid_issuer_signed_item_input)

        assert actual.digest_id == valid_issuer_signed_item.digest_id
        assert actual.element_identifier == valid_issuer_signed_item.element_identifier
        assert actual.element_value == valid_issuer_signed_item.element_value
        assert actual.random == valid_issuer_signed_item.random

    @pytest.mark.parametrize(
        "invalid_input",
        [
            {},
            {"digestID": 1},
            {"digestID": 1, "elementIdentifier": ""}
        ],
        ids = [
            "Missing digest identifier",
            "Missing element identifier",
            "Missing element value"
        ]
    )
    def test_invalid_input(
        self,
        invalid_input: Dict[AnyStr, Any]
    ):
        with pytest.raises(KeyError):
            IssuerSignedItem.from_dict(invalid_input)
