import cbor2
import pytest
from datetime import datetime

from mock_credential.mso import MSO, SAMPLE_MSO
from mock_credential.namespaces import SAMPLE_NAMESPACES


class TestMSOFromDict:

    def test_returns_mso_instance(self, device_cose_key):
        result = MSO.from_dict(SAMPLE_MSO, device_cose_key)
        assert isinstance(result, MSO)

    def test_sets_doc_type_from_config(self, device_cose_key):
        result = MSO.from_dict(SAMPLE_MSO, device_cose_key)
        assert result.doc_type == SAMPLE_MSO["doc_type"]

    def test_sets_validity_days(self, device_cose_key):
        result = MSO.from_dict(SAMPLE_MSO, device_cose_key, validity_days=180)
        assert result.validity_days == 180

    def test_default_validity_days_is_365(self, device_cose_key):
        result = MSO.from_dict(SAMPLE_MSO, device_cose_key)
        assert result.validity_days == 365

    @pytest.mark.parametrize(
        "invalid_config",
        [
            {"namespaces": SAMPLE_MSO["namespaces"]},
            {"doc_type": SAMPLE_MSO["doc_type"]},
        ],
        ids=[
            "Missing doc_type",
            "Missing namespaces",
        ]
    )
    def test_missing_key_raises(self, invalid_config, device_cose_key):
        with pytest.raises(KeyError):
            MSO.from_dict(invalid_config, device_cose_key)


class TestMSOBuildMsoDict:

    def test_contains_required_keys(self, mso, now):
        result = mso.build_mso_dict(now)
        assert "version" in result
        assert "digestAlgorithm" in result
        assert "valueDigests" in result
        assert "deviceKeyInfo" in result
        assert "docType" in result
        assert "validityInfo" in result

    def test_version_is_1_0(self, mso, now):
        result = mso.build_mso_dict(now)
        assert result["version"] == "1.0"

    def test_digest_algorithm_is_sha256(self, mso, now):
        result = mso.build_mso_dict(now)
        assert result["digestAlgorithm"] == "SHA-256"

    def test_doc_type_matches(self, mso, now):
        result = mso.build_mso_dict(now)
        assert result["docType"] == SAMPLE_MSO["doc_type"]

    def test_validity_info_signed_uses_provided_now(self, mso, now):
        result = mso.build_mso_dict(now)
        assert result["validityInfo"]["signed"].value == "2025-01-01T00:00:00Z"

    def test_device_key_info_contains_device_key(self, mso, now, device_cose_key):
        result = mso.build_mso_dict(now)
        assert result["deviceKeyInfo"]["deviceKey"] == device_cose_key


class TestMSOBuildTaggedBytes:

    def test_returns_bytes(self, mso, now):
        result = mso.build_tagged_bytes(now)
        assert isinstance(result, bytes)

    def test_outer_tag_is_cbor(self, mso, now):
        result = mso.build_tagged_bytes(now)
        decoded = cbor2.loads(result)
        assert isinstance(decoded, cbor2.CBORTag)

    def test_inner_tag_is_24(self, mso, now):
        result = mso.build_tagged_bytes(now)
        decoded = cbor2.loads(result)
        assert decoded.tag == 24

    def test_inner_value_is_valid_cbor(self, mso, now):
        result = mso.build_tagged_bytes(now)
        decoded = cbor2.loads(result)
        inner = cbor2.loads(decoded.value)
        assert isinstance(inner, dict)
