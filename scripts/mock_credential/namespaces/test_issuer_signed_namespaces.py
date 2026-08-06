import pytest
from mock_credential.namespaces import SAMPLE_NAMESPACES

class TestIssuerSignedNamespaces:

    def test_sample_items_have_two_keys(self):
        assert len(SAMPLE_NAMESPACES) == 2


    @pytest.mark.parametrize(
        "namespace, expected_length",
        [
            ("org.iso.18013.5.1", 11),
            ("org.iso.18013.5.1.GB", 1)
        ],
        ids = [
            "Common ISO namespace",
            "British domestic ISO namespace"
        ]
    )
    def test_sample_items_contains_entries(self, namespace, expected_length):
        namespaces = SAMPLE_NAMESPACES[namespace]
        assert namespaces is not None
        assert len(namespaces) == expected_length


    @pytest.mark.parametrize(
        "namespace, expected_length",
        [
            ("org.iso.18013.5.1", 11),
            ("org.iso.18013.5.1.GB", 1)
        ],
        ids = [
            "Common ISO namespace",
            "British domestic ISO namespace"
        ]
    )
    def test_build_issuer_signed_items(self, namespace, expected_length):
        items = SAMPLE_NAMESPACES.build_issuer_signed_items()

        assert len(items) == 2
        assert len(items[namespace]) == expected_length


    @pytest.mark.parametrize(
        "namespace, expected_length",
        [
            ("org.iso.18013.5.1", 11),
            ("org.iso.18013.5.1.GB", 1)
        ],
        ids = [
            "Common ISO namespace",
            "British domestic ISO namespace"
        ]
    )
    def test_value_digest_generation(self, namespace, expected_length):
        digests = SAMPLE_NAMESPACES.as_value_digests()

        assert digests is not None
        assert len(digests) == 2
        assert len(digests[namespace]) == expected_length