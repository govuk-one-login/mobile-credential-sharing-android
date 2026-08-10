from cbor2 import dumps, loads, CBORTag
from hashlib import sha256
from typing import AnyStr, Dict, List, Type, TypeVar
from mock_credential.namespaces import IssuerSignedItem

T = TypeVar('T', bound='Parent') # type: ignore


class IssuerSignedNamespaces:

    def __init__(self, namespaces: Dict):
        self.namespaces: Dict[str, List[IssuerSignedItem]] = {
            ns_name: [
                IssuerSignedItem.from_dict(item)
                for item
                in items
            ]
            for ns_name, items
            in namespaces.items()
        }


    def __getitem__(self, key: str):
        return self.namespaces[key]

    def __iter__(self):
        return iter(self.namespaces)

    def __len__(self) -> int:
        return len(self.namespaces.keys())

    def build_issuer_signed_items(self) -> Dict[AnyStr, CBORTag]:
        return {
            namespace_key: [
                item.as_tagged_cbor()
                for item
                in items
            ]
            for namespace_key, items
            in self.namespaces.items()
        }

    def as_value_digests(self) -> Dict:
        return {
            ns_name: {
                item.digest_id: item.as_value_digest()
                for item
                in items
            }
            for ns_name, items
            in self.namespaces.items()
        }


    @classmethod
    def from_dict(cls: Type[T], args: Dict) -> T:
        return cls(
            namespaces = args
        )