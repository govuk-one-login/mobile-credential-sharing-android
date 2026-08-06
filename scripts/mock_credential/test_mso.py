import cbor2
import pytest
from datetime import datetime, timezone

from mock_credential.mso import MSO
from mock_credential.namespaces import MSO_CONFIG, SAMPLE_NAMESPACES


DEVICE_COSE_KEY = {1: 2, -1: 1, -2: b"\x00" * 32, -3: b"\x00" * 32}

NOW = datetime(2025, 1, 1, tzinfo=timezone.utc)


class TestMSOFromDict:

    def test_returns_mso_instance(self):
        mso = MSO.from_dict(MSO_CONFIG, DEVICE_COSE_KEY)
        assert isinstance(mso, MSO)

    def test_sets_doc_type_from_config(self):
        mso = MSO.from_dict(MSO_CONFIG, DEVICE_COSE_KEY)
        assert mso.doc_type == MSO_CONFIG["doc_type"]

    def test_sets_validity_days(self):
        mso = MSO.from_dict(MSO_CONFIG, DEVICE_COSE_KEY, validity_days=180)
        assert mso.validity_days == 180

    def test_default_validity_days_is_365(self):
        mso = MSO.from_dict(MSO_CONFIG, DEVICE_COSE_KEY)
        assert mso.validity_days == 365

    def test_missing_doc_type_raises(self):
        with pytest.raises(KeyError):
            MSO.from_dict({"namespaces": MSO_CONFIG["namespaces"]}, DEVICE_COSE_KEY)

    def test_missing_namespaces_raises(self):
        with pytest.raises(KeyError):
            MSO.from_dict({"doc_type": MSO_CONFIG["doc_type"]}, DEVICE_COSE_KEY)


class TestMSOBuildMsoDict:

    def test_contains_required_keys(self):
        mso = MSO(SAMPLE_NAMESPACES, DEVICE_COSE_KEY)
        result = mso.build_mso_dict(NOW)
        assert "version" in result
        assert "digestAlgorithm" in result
        assert "valueDigests" in result
        assert "deviceKeyInfo" in result
        assert "docType" in result
        assert "validityInfo" in result

    def test_version_is_1_0(self):
        mso = MSO(SAMPLE_NAMESPACES, DEVICE_COSE_KEY)
        result = mso.build_mso_dict(NOW)
        assert result["version"] == "1.0"

    def test_digest_algorithm_is_sha256(self):
        mso = MSO(SAMPLE_NAMESPACES, DEVICE_COSE_KEY)
        result = mso.build_mso_dict(NOW)
        assert result["digestAlgorithm"] == "SHA-256"

    def test_doc_type_matches(self):
        mso = MSO(SAMPLE_NAMESPACES, DEVICE_COSE_KEY, doc_type="org.iso.18013.5.1.mDL")
        result = mso.build_mso_dict(NOW)
        assert result["docType"] == "org.iso.18013.5.1.mDL"

    def test_validity_info_signed_uses_provided_now(self):
        mso = MSO(SAMPLE_NAMESPACES, DEVICE_COSE_KEY)
        result = mso.build_mso_dict(NOW)
        assert result["validityInfo"]["signed"].value == "2025-01-01T00:00:00Z"

    def test_device_key_info_contains_device_key(self):
        mso = MSO(SAMPLE_NAMESPACES, DEVICE_COSE_KEY)
        result = mso.build_mso_dict(NOW)
        assert result["deviceKeyInfo"]["deviceKey"] == DEVICE_COSE_KEY


class TestMSOBuildTaggedBytes:

    def test_returns_bytes(self):
        mso = MSO(SAMPLE_NAMESPACES, DEVICE_COSE_KEY)
        result = mso.build_tagged_bytes(NOW)
        assert isinstance(result, bytes)

    def test_outer_tag_is_cbor(self):
        mso = MSO(SAMPLE_NAMESPACES, DEVICE_COSE_KEY)
        result = mso.build_tagged_bytes(NOW)
        decoded = cbor2.loads(result)
        assert isinstance(decoded, cbor2.CBORTag)

    def test_inner_tag_is_24(self):
        mso = MSO(SAMPLE_NAMESPACES, DEVICE_COSE_KEY)
        result = mso.build_tagged_bytes(NOW)
        decoded = cbor2.loads(result)
        assert decoded.tag == 24

    def test_inner_value_is_valid_cbor(self):
        mso = MSO(SAMPLE_NAMESPACES, DEVICE_COSE_KEY)
        result = mso.build_tagged_bytes(NOW)
        decoded = cbor2.loads(result)
        inner = cbor2.loads(decoded.value)
        assert isinstance(inner, dict)
