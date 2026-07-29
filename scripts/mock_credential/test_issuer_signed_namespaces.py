import pytest
from mock_credential.namespaces import SAMPLE_NAMESPACES

class TestIssuerSignedNamespaces:

    def test_sample_items_have_two_keys(self):
        assert len(SAMPLE_NAMESPACES) == 2

    @pytest.mark.parametrize(
        "namespace",
        [
            "org.iso.18013.5.1",
            "org.iso.18013.5.1.GB"
        ],
        ids = [
            "Common ISO namespace",
            "British domestic ISO namespace"
        ]
    )
    def test_sample_items_contains_entries(self, namespace):
        assert SAMPLE_NAMESPACES[namespace] is not None