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

        assert actual == valid_issuer_signed_item

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
