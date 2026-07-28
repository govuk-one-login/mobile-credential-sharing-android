from cbor2 import dumps, CBORTag
from hashlib import sha256
from typing import Dict, Type, TypeVar

T = TypeVar('T', bound='Parent') # type: ignore

class IssuerSignedItem:
    def __init__(self, **kwargs):
        self.digest_id = kwargs["digest_id"]
        self.element_identifier = kwargs["element_identifier"]
        self.element_value = kwargs["element_value"]
        if "random" in kwargs:
            self.random = kwargs["random"]
        else:
            self.random = sha256(str(self.digest_id).encode()).digest()[:16]

    def __eq__(self, other):
        return (
            isinstance(other, IssuerSignedItem)
            and (self.digest_id == other.digest_id)
            and (self.element_identifier == other.element_identifier)
            and (self.element_value == other.element_value)
            and (self.random == other.random)
        )

    def build_issuer_signed_item(self):
        """Encode an IssuerSignedItem as Tag(24, bstr(encoded_item))."""
        item_with_random = {
            "digestID": self.digest_id,
            "random": self.random,
            "elementIdentifier": self.element_identifier,
            "elementValue": self.element_value,
        }
        encoded = dumps(item_with_random)
        return CBORTag(24, encoded)

    @classmethod
    def from_dict(cls: Type[T], args: Dict) -> T:
        return cls(
            digest_id = args["digestID"],
            element_identifier = args["elementIdentifier"],
            element_value = args["elementValue"]
        )