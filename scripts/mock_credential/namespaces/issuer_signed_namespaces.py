from cbor2 import dumps, loads
from hashlib import sha256
from typing import Dict, Type, TypeVar
from mock_credential.namespaces import IssuerSignedItem

T = TypeVar('T', bound='Parent') # type: ignore


class IssuerSignedNamespaces:

    def __init__(self, namespaces: Dict):
        self.namespaces: Dict = {}
        for ns_name, items in namespaces.items():
            self.namespaces[ns_name] = [
                IssuerSignedItem.from_dict(item)
                for item
                in items
        ]

    def __getitem__(self, key: str):
        return self.namespaces[key]

    def __iter__(self):
        return iter(self.namespaces)

    def __len__(self) -> int:
        return len(self.namespaces.keys())

    def as_value_digests(self) -> Dict:
        value_digests = {}
        for ns_name, items in self.namespaces.items():
            ns_digests = {}
            for item in items:
                decoded_item = item.load_issuer_signed_item()
                ns_digests[decoded_item["digestID"]] = item.as_value_digest()
                value_digests[ns_name] = ns_digests

        return value_digests

    @classmethod
    def from_dict(cls: Type[T], args: Dict) -> T:
        return cls(
            namespaces = args
        )