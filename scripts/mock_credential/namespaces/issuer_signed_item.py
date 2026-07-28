from hashlib import sha256
from typing import Dict, Type, TypeVar

T = TypeVar('T', bound='Parent') # type: ignore

class IssuerSignedItem:
    def __init__(self, **kwargs):
        self.digest_id = kwargs["digest_id"]
        self.element_identifier = kwargs["element_identifier"]
        self.element_value = kwargs["element_value"]
        self.random = sha256(str(self.digest_id).encode()).digest()[:16],

    @classmethod
    def from_dict(cls: Type[T], args: Dict) -> T:
        return cls(
            digest_id = args["digestID"],
            element_identifier = args["elementIdentifier"],
            element_value = args["elementValue"]
        )