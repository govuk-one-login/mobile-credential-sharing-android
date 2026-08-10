import cbor2
from datetime import datetime, timedelta
from typing import Dict, Type, TypeVar
from ..namespaces import IssuerSignedNamespaces, SAMPLE_ITEMS

T = TypeVar("T", bound="MSO")

SAMPLE_MSO = {
    "doc_type": "org.iso.18013.5.1.mDL",
    "namespaces": SAMPLE_ITEMS,
}


class MSO:
    def __init__(
        self,
        namespaces: IssuerSignedNamespaces,
        device_cose_key: Dict,
        doc_type: str = "org.iso.18013.5.1.mDL",
        validity_days: int = 365
    ):
        self.namespaces = namespaces
        self.device_cose_key = device_cose_key
        self.doc_type = doc_type
        self.validity_days = validity_days

    @classmethod
    def from_dict(cls: Type[T], config: Dict, device_cose_key: Dict, validity_days: int = 365) -> T:
        """Constructs an MSO from a config dict containing 'doc_type' and 'namespaces'."""
        namespaces = IssuerSignedNamespaces.from_dict(config["namespaces"])
        return cls(
            namespaces=namespaces,
            device_cose_key=device_cose_key,
            doc_type=config["doc_type"],
            validity_days=validity_days,
        )

    def build_mso_dict(self, now: datetime) -> Dict:
        """Constructs the MSO dictionary structure."""
        return {
            "version": "1.0",
            "digestAlgorithm": "SHA-256",
            "valueDigests": self.namespaces.as_value_digests(),
            "deviceKeyInfo": {
                "deviceKey": self.device_cose_key,
                "keyAuthorizations": {
                    "nameSpaces": list(self.namespaces)
                },
            },
            "docType": self.doc_type,
            "validityInfo": {
                "signed": cbor2.CBORTag(0, now.strftime("%Y-%m-%dT%H:%M:%SZ")),
                "validFrom": cbor2.CBORTag(0, now.strftime("%Y-%m-%dT%H:%M:%SZ")),
                "validUntil": cbor2.CBORTag(0, (now + timedelta(days=self.validity_days)).strftime(
                    "%Y-%m-%dT%H:%M:%SZ")),
            },
        }

    def build_tagged_bytes(self, now: datetime) -> bytes:
        """Returns CBOR Tag 24 encoded MSO bytes."""
        mso_dict = self.build_mso_dict(now)
        mso_bytes = cbor2.dumps(mso_dict)
        return cbor2.dumps(cbor2.CBORTag(24, mso_bytes))
