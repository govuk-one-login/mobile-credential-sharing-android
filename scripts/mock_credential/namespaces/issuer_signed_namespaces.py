from typing import Dict, Type, TypeVar
from mock_credential.namespaces import IssuerSignedItem

T = TypeVar('T', bound='Parent') # type: ignore


class IssuerSignedNamespaces:

    def __init__(self, namespaces: Dict):
        self.namespaces: Dict = {}
        for ns_name, items in namespaces.items():
            self.namespaces[ns_name] = [
                IssuerSignedItem.from_dict(item).build_issuer_signed_item()
                for item
                in items
        ]

    def __getitem__(self, key: str):
        return self.namespaces[key]

    def __iter__(self):
        return iter(self.namespaces)

    def __len__(self) -> int:
        return len(self.namespaces.keys())

    @classmethod
    def from_dict(cls: Type[T], args: Dict) -> T:
        return cls(
            namespaces = args
        )