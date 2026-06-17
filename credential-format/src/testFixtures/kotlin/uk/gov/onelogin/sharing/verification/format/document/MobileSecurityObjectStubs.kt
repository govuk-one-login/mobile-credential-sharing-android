package uk.gov.onelogin.sharing.verification.format.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory

object MobileSecurityObjectStubs {

    const val DEFAULT_VERSION = "1.0"
    const val DEFAULT_DIGEST_ALGORITHM = "SHA-256"
    const val DEFAULT_DOC_TYPE = "org.iso.18013.5.1.mDL"
    const val DEFAULT_NAMESPACE = "org.iso.18013.5.1"
    const val DEFAULT_SIGNED = "2024-01-15T10:00:00Z"
    const val DEFAULT_VALID_FROM = "2024-01-15T10:00:00Z"
    const val DEFAULT_VALID_UNTIL = "2025-01-15T10:00:00Z"
    const val DEFAULT_EXPECTED_UPDATE = "2024-06-15T10:00:00Z"

    private const val KEY_VERSION = "version"
    private const val KEY_DIGEST_ALGORITHM = "digestAlgorithm"
    private const val KEY_DOC_TYPE = "docType"
    private const val KEY_VALUE_DIGESTS = "valueDigests"
    private const val KEY_DEVICE_KEY_INFO = "deviceKeyInfo"
    private const val KEY_DEVICE_KEY = "deviceKey"
    private const val KEY_VALIDITY_INFO = "validityInfo"
    private const val KEY_SIGNED = "signed"
    private const val KEY_VALID_FROM = "validFrom"
    private const val KEY_VALID_UNTIL = "validUntil"
    private const val KEY_EXPECTED_UPDATE = "expectedUpdate"
    private const val KEY_STATUS = "status"

    private val cborMapper = ObjectMapper(CBORFactory())

    val validEncodedMSO: ByteArray = wrapTag24(buildValidMsoBytes())

    val malformedEncodedMSO: ByteArray = byteArrayOf(0xFF.toByte(), 0x01, 0x02)

    val encodedMsoWithInvalidVersion: ByteArray = wrapTag24(
        buildMsoBytes(version = "2.0")
    )

    val encodedMsoWithMismatchedDigests: ByteArray = wrapTag24(
        buildMsoBytes(
            valueDigests = mapOf(DEFAULT_NAMESPACE to mapOf(99 to byteArrayOf(0xAA.toByte())))
        )
    )

    val encodedMsoWithNumericOffset: ByteArray = wrapTag24(
        buildMsoBytes(signed = "2024-01-15T10:00:00+00:00")
    )

    val encodedMsoWithFractionalSeconds: ByteArray = wrapTag24(
        buildMsoBytes(signed = "2024-01-15T10:00:00.123Z")
    )

    val encodedMsoWithDuplicateKeys: ByteArray = wrapTag24(buildDuplicateKeyMsoBytes())

    private fun buildValidMsoBytes(): ByteArray = buildMsoBytes()

    @Suppress("LongParameterList")
    fun buildMsoBytes(
        version: String = DEFAULT_VERSION,
        digestAlgorithm: String = DEFAULT_DIGEST_ALGORITHM,
        docType: String = DEFAULT_DOC_TYPE,
        valueDigests: Map<String, Map<Int, ByteArray>> = mapOf(
            DEFAULT_NAMESPACE to mapOf(0 to byteArrayOf(0x01, 0x02))
        ),
        signed: String = DEFAULT_SIGNED,
        validFrom: String = DEFAULT_VALID_FROM,
        validUntil: String = DEFAULT_VALID_UNTIL,
        includeExpectedUpdate: String? = null,
        includeStatus: ByteArray? = null
    ): ByteArray {
        val root = cborMapper.createObjectNode()
        root.put(KEY_VERSION, version)
        root.put(KEY_DIGEST_ALGORITHM, digestAlgorithm)
        root.put(KEY_DOC_TYPE, docType)

        val vdNode = cborMapper.createObjectNode()
        valueDigests.forEach { (ns, digests) ->
            val nsNode = cborMapper.createObjectNode()
            digests.forEach { (id, bytes) -> nsNode.put(id.toString(), bytes) }
            vdNode.set<ObjectNode>(ns, nsNode)
        }
        root.set<ObjectNode>(KEY_VALUE_DIGESTS, vdNode)

        val dki = cborMapper.createObjectNode()
        val deviceKey = cborMapper.createObjectNode()
        deviceKey.put("1", 2)
        deviceKey.put("-1", 1)
        deviceKey.put("-2", byteArrayOf(0x01, 0x02, 0x03))
        deviceKey.put("-3", byteArrayOf(0x04, 0x05, 0x06))
        dki.set<ObjectNode>(KEY_DEVICE_KEY, deviceKey)
        root.set<ObjectNode>(KEY_DEVICE_KEY_INFO, dki)

        val vi = cborMapper.createObjectNode()
        vi.put(KEY_SIGNED, signed)
        vi.put(KEY_VALID_FROM, validFrom)
        vi.put(KEY_VALID_UNTIL, validUntil)
        includeExpectedUpdate?.let { vi.put(KEY_EXPECTED_UPDATE, it) }
        root.set<ObjectNode>(KEY_VALIDITY_INFO, vi)

        includeStatus?.let { root.put(KEY_STATUS, it) }

        return cborMapper.writeValueAsBytes(root)
    }

    /**
     * Hand-crafted CBOR bytes with duplicate "version" key in the top-level map.
     */
    private fun buildDuplicateKeyMsoBytes(): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val g = CBORFactory().createGenerator(output)
        g.writeStartObject()
        g.writeStringField(KEY_VERSION, DEFAULT_VERSION)
        g.writeStringField(KEY_VERSION, DEFAULT_VERSION)
        g.writeStringField(KEY_DIGEST_ALGORITHM, DEFAULT_DIGEST_ALGORITHM)
        g.writeStringField(KEY_DOC_TYPE, DEFAULT_DOC_TYPE)
        g.writeFieldName(KEY_VALUE_DIGESTS)
        g.writeStartObject()
        g.writeFieldName(DEFAULT_NAMESPACE)
        g.writeStartObject()
        g.writeFieldName("0")
        g.writeBinary(byteArrayOf(0x01, 0x02))
        g.writeEndObject()
        g.writeEndObject()
        g.writeFieldName(KEY_DEVICE_KEY_INFO)
        g.writeStartObject()
        g.writeFieldName(KEY_DEVICE_KEY)
        g.writeStartObject()
        g.writeFieldName("1")
        g.writeNumber(2)
        g.writeEndObject()
        g.writeEndObject()
        g.writeFieldName(KEY_VALIDITY_INFO)
        g.writeStartObject()
        g.writeStringField(KEY_SIGNED, DEFAULT_SIGNED)
        g.writeStringField(KEY_VALID_FROM, DEFAULT_VALID_FROM)
        g.writeStringField(KEY_VALID_UNTIL, DEFAULT_VALID_UNTIL)
        g.writeEndObject()
        g.writeEndObject()
        g.close()
        return output.toByteArray()
    }

    fun wrapTag24(innerBytes: ByteArray): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val g = CBORFactory().createGenerator(output)
        g.writeTag(24)
        g.writeBinary(innerBytes)
        g.close()
        return output.toByteArray()
    }

    private fun buildMsoBytesWithoutField(excludedField: String): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val g = CBORFactory().createGenerator(output)
        g.writeStartObject()
        if (excludedField != KEY_VERSION) g.writeStringField(KEY_VERSION, DEFAULT_VERSION)
        if (excludedField != KEY_DIGEST_ALGORITHM) {
            g.writeStringField(KEY_DIGEST_ALGORITHM, DEFAULT_DIGEST_ALGORITHM)
        }
        if (excludedField != KEY_DOC_TYPE) g.writeStringField(KEY_DOC_TYPE, DEFAULT_DOC_TYPE)
        if (excludedField != KEY_VALUE_DIGESTS) {
            g.writeFieldName(KEY_VALUE_DIGESTS)
            g.writeStartObject()
            g.writeFieldName(DEFAULT_NAMESPACE)
            g.writeStartObject()
            g.writeFieldName("0")
            g.writeBinary(byteArrayOf(0x01, 0x02))
            g.writeEndObject()
            g.writeEndObject()
        }
        if (excludedField != KEY_DEVICE_KEY_INFO) {
            g.writeFieldName(KEY_DEVICE_KEY_INFO)
            g.writeStartObject()
            g.writeFieldName(KEY_DEVICE_KEY)
            g.writeStartObject()
            g.writeFieldName("1")
            g.writeNumber(2)
            g.writeEndObject()
            g.writeEndObject()
        }
        if (excludedField != KEY_VALIDITY_INFO) {
            g.writeFieldName(KEY_VALIDITY_INFO)
            g.writeStartObject()
            g.writeStringField(KEY_SIGNED, DEFAULT_SIGNED)
            g.writeStringField(KEY_VALID_FROM, DEFAULT_VALID_FROM)
            g.writeStringField(KEY_VALID_UNTIL, DEFAULT_VALID_UNTIL)
            g.writeEndObject()
        }
        g.writeEndObject()
        g.close()
        return output.toByteArray()
    }

    fun buildMsoBytesWithNonIntegerDigestKeys(): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val g = CBORFactory().createGenerator(output)
        g.writeStartObject()
        g.writeStringField(KEY_VERSION, DEFAULT_VERSION)
        g.writeStringField(KEY_DIGEST_ALGORITHM, DEFAULT_DIGEST_ALGORITHM)
        g.writeStringField(KEY_DOC_TYPE, DEFAULT_DOC_TYPE)
        g.writeFieldName(KEY_VALUE_DIGESTS)
        g.writeStartObject()
        g.writeFieldName(DEFAULT_NAMESPACE)
        g.writeStartObject()
        g.writeFieldName("not_a_number")
        g.writeBinary(byteArrayOf(0x01, 0x02))
        g.writeEndObject()
        g.writeEndObject()
        g.writeFieldName(KEY_DEVICE_KEY_INFO)
        g.writeStartObject()
        g.writeFieldName(KEY_DEVICE_KEY)
        g.writeStartObject()
        g.writeFieldName("1")
        g.writeNumber(2)
        g.writeEndObject()
        g.writeEndObject()
        g.writeFieldName(KEY_VALIDITY_INFO)
        g.writeStartObject()
        g.writeStringField(KEY_SIGNED, DEFAULT_SIGNED)
        g.writeStringField(KEY_VALID_FROM, DEFAULT_VALID_FROM)
        g.writeStringField(KEY_VALID_UNTIL, DEFAULT_VALID_UNTIL)
        g.writeEndObject()
        g.writeEndObject()
        g.close()
        return output.toByteArray()
    }

    private fun buildMsoBytesWithEmptyDeviceKeyInfo(): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val g = CBORFactory().createGenerator(output)
        g.writeStartObject()
        g.writeStringField(KEY_VERSION, DEFAULT_VERSION)
        g.writeStringField(KEY_DIGEST_ALGORITHM, DEFAULT_DIGEST_ALGORITHM)
        g.writeStringField(KEY_DOC_TYPE, DEFAULT_DOC_TYPE)
        g.writeFieldName(KEY_VALUE_DIGESTS)
        g.writeStartObject()
        g.writeFieldName(DEFAULT_NAMESPACE)
        g.writeStartObject()
        g.writeFieldName("0")
        g.writeBinary(byteArrayOf(0x01, 0x02))
        g.writeEndObject()
        g.writeEndObject()
        g.writeFieldName(KEY_DEVICE_KEY_INFO)
        g.writeStartObject()
        g.writeEndObject()
        g.writeFieldName(KEY_VALIDITY_INFO)
        g.writeStartObject()
        g.writeStringField(KEY_SIGNED, DEFAULT_SIGNED)
        g.writeStringField(KEY_VALID_FROM, DEFAULT_VALID_FROM)
        g.writeStringField(KEY_VALID_UNTIL, DEFAULT_VALID_UNTIL)
        g.writeEndObject()
        g.writeEndObject()
        g.close()
        return output.toByteArray()
    }

    val encodedMsoWithExpectedUpdate: ByteArray = wrapTag24(
        buildMsoBytes(includeExpectedUpdate = DEFAULT_EXPECTED_UPDATE)
    )

    val encodedMsoWithInvalidExpectedUpdate: ByteArray = wrapTag24(
        buildMsoBytes(includeExpectedUpdate = "2024-06-15T10:00:00+01:00")
    )

    val encodedMsoWithStatus: ByteArray = wrapTag24(
        buildMsoBytes(includeStatus = byteArrayOf(0x01, 0x02))
    )

    val encodedMsoWithMultipleNamespaces: ByteArray = wrapTag24(
        buildMsoBytes(
            valueDigests = mapOf(
                DEFAULT_NAMESPACE to mapOf(0 to byteArrayOf(0x01)),
                "org.iso.18013.5.1.aamva" to mapOf(1 to byteArrayOf(0x02))
            )
        )
    )

    val encodedMsoWithMissingVersion: ByteArray = wrapTag24(
        buildMsoBytesWithoutField(KEY_VERSION)
    )

    val encodedMsoWithMissingDocType: ByteArray = wrapTag24(
        buildMsoBytesWithoutField(KEY_DOC_TYPE)
    )

    val encodedMsoWithMissingValueDigests: ByteArray = wrapTag24(
        buildMsoBytesWithoutField(KEY_VALUE_DIGESTS)
    )

    val encodedMsoWithMissingDeviceKeyInfo: ByteArray = wrapTag24(
        buildMsoBytesWithoutField(KEY_DEVICE_KEY_INFO)
    )

    val encodedMsoWithMissingValidityInfo: ByteArray = wrapTag24(
        buildMsoBytesWithoutField(KEY_VALIDITY_INFO)
    )

    val encodedMsoWithMissingDigestAlgorithm: ByteArray = wrapTag24(
        buildMsoBytesWithoutField(KEY_DIGEST_ALGORITHM)
    )

    val encodedMsoWithNonIntegerDigestKeys: ByteArray = wrapTag24(
        buildMsoBytesWithNonIntegerDigestKeys()
    )

    val encodedMsoWithEmptyDeviceKeyInfo: ByteArray = wrapTag24(
        buildMsoBytesWithEmptyDeviceKeyInfo()
    )

    val encodedMsoNotTag24Wrapped: ByteArray = buildMsoBytes()

    val encodedMsoWithUnsupportedAlgorithm: ByteArray = wrapTag24(
        buildMsoBytes(digestAlgorithm = "SHA-512")
    )
}
