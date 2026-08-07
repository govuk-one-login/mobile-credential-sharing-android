import os
from base64 import b64decode
from cbor2 import CBORTag
from .issuer_signed_item import IssuerSignedItem
from .issuer_signed_namespaces import IssuerSignedNamespaces

# Sample IssuerSignedItems for a test mDL
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PORTRAIT_BYTES = b64decode(
    open(os.path.join(SCRIPT_DIR, "portrait.txt")).read().strip()
)

SAMPLE_ITEMS = {
    "org.iso.18013.5.1": [
        {"digestID": 1, "elementIdentifier": "family_name", "elementValue": "Doe"},
        {"digestID": 2, "elementIdentifier": "given_name", "elementValue": "Jane"},
        {"digestID": 3, "elementIdentifier": "portrait", "elementValue": PORTRAIT_BYTES},
        {"digestID": 4, "elementIdentifier": "birth_date",
         "elementValue": CBORTag(1004, "2007-01-15")},
        {"digestID": 5, "elementIdentifier": "issue_date",
         "elementValue": CBORTag(1004, "2024-01-01")},
        {"digestID": 6, "elementIdentifier": "expiry_date",
         "elementValue": CBORTag(1004, "2034-01-01")},
        {"digestID": 7, "elementIdentifier": "issuing_country", "elementValue": "GB"},
        {"digestID": 8, "elementIdentifier": "issuing_authority", "elementValue": "DVLA"},
        {"digestID": 9, "elementIdentifier": "age_over_18", "elementValue": True},
        {"digestID": 10, "elementIdentifier": "age_over_21", "elementValue": False},
        {"digestID": 11, "elementIdentifier": "age_over_25", "elementValue": False},
    ],
    "org.iso.18013.5.1.GB": [
        {"digestID": 12, "elementIdentifier": "welsh_licence", "elementValue": False},
    ],
}

SAMPLE_NAMESPACES: IssuerSignedNamespaces = IssuerSignedNamespaces.from_dict(SAMPLE_ITEMS)