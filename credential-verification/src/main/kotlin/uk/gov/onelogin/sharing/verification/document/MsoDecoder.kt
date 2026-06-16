package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObject
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceKeyInfo
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.validity.ValidityInfo

@OptIn(ExperimentalTime::class)
internal class MsoDecoder {

    private val cborFactory = CBORFactory()
    private val cborMapper = ObjectMapper(cborFactory)

    fun decode(encodedMso: ByteArray): MobileSecurityObject = try {
        val innerBytes = unwrapTag24(encodedMso)
        checkDuplicateKeys(innerBytes)
        parseMso(innerBytes)
    } catch (e: VerificationResult.Failure) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
        throw malformed
    }

    private fun unwrapTag24(data: ByteArray): ByteArray {
        val root = cborMapper.readTree(data)
        return (root as? BinaryNode)?.binaryValue() ?: throw malformed
    }

    private fun checkDuplicateKeys(data: ByteArray) {
        (cborFactory.createParser(data) as CBORParser).use { parser ->
            while (parser.nextToken() != null) {
                when (parser.currentToken()) {
                    JsonToken.START_OBJECT -> scanObject(parser)
                    else -> {}
                }
            }
        }
    }

    private fun scanObject(parser: CBORParser) {
        val keys = mutableSetOf<String>()
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            when (parser.currentToken()) {
                JsonToken.FIELD_NAME -> {
                    if (!keys.add(parser.currentName())) throw malformed
                }

                JsonToken.START_OBJECT -> scanObject(parser)

                JsonToken.START_ARRAY -> scanArray(parser)

                else -> {}
            }
        }
    }

    private fun scanArray(parser: CBORParser) {
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            when (parser.currentToken()) {
                JsonToken.START_OBJECT -> scanObject(parser)
                JsonToken.START_ARRAY -> scanArray(parser)
                else -> {}
            }
        }
    }

    @Suppress("ThrowsCount")
    private fun parseMso(data: ByteArray): MobileSecurityObject {
        val root = cborMapper.readTree(data)

        val version = root.get(KEY_VERSION)?.asText() ?: throw malformed
        val digestAlgorithm = root.get(KEY_DIGEST_ALGORITHM)?.asText() ?: throw malformed
        val docType = root.get(KEY_DOC_TYPE)?.asText() ?: throw malformed
        val valueDigests = parseValueDigests(root.get(KEY_VALUE_DIGESTS) ?: throw malformed)
        val deviceKeyInfo = parseDeviceKeyInfo(root.get(KEY_DEVICE_KEY_INFO) ?: throw malformed)
        val validityInfo = parseValidityInfo(root.get(KEY_VALIDITY_INFO) ?: throw malformed)
        val status = (root.get(KEY_STATUS) as? BinaryNode)?.binaryValue()

        return MobileSecurityObject(
            version = version,
            digestAlgorithm = digestAlgorithm,
            docType = docType,
            valueDigests = valueDigests,
            deviceKeyInfo = deviceKeyInfo,
            validityInfo = validityInfo,
            status = status
        )
    }

    private fun parseValueDigests(node: JsonNode): Map<String, Map<Int, ByteArray>> {
        val result = mutableMapOf<String, Map<Int, ByteArray>>()
        node.properties().forEach { (namespace, digestsNode) ->
            val digests = mutableMapOf<Int, ByteArray>()
            digestsNode.properties().forEach { (id, value) ->
                val digestId = id.toIntOrNull() ?: throw malformed
                digests[digestId] = (value as? BinaryNode)?.binaryValue() ?: throw malformed
            }
            result[namespace] = digests
        }
        return result
    }

    private fun parseDeviceKeyInfo(node: JsonNode): DeviceKeyInfo {
        val deviceKeyNode = node.get(KEY_DEVICE_KEY) ?: throw malformed
        val deviceKeyBytes = cborMapper.writeValueAsBytes(deviceKeyNode)
        return DeviceKeyInfo(deviceKey = deviceKeyBytes)
    }

    private fun parseValidityInfo(node: JsonNode): ValidityInfo {
        val signed = node.requireTimestamp(KEY_SIGNED)
        val validFrom = node.requireTimestamp(KEY_VALID_FROM)
        val validUntil = node.requireTimestamp(KEY_VALID_UNTIL)
        val expectedUpdate = node.get(KEY_EXPECTED_UPDATE)?.asText()
            ?.also { if (!TIMESTAMP_PATTERN.matches(it)) throw malformed }
            ?.let { Instant.parse(it) }

        return ValidityInfo(
            signed = signed,
            validFrom = validFrom,
            validUntil = validUntil,
            expectedUpdate = expectedUpdate
        )
    }

    private fun JsonNode.requireTimestamp(key: String): Instant {
        val value = get(key)?.asText() ?: throw malformed
        if (!TIMESTAMP_PATTERN.matches(value)) throw malformed
        return Instant.parse(value)
    }

    private val malformed
        get() = VerificationResult.Failure(VerificationError.MALFORMED_MSO)

    companion object {
        private const val KEY_VERSION = "version"
        private const val KEY_DIGEST_ALGORITHM = "digestAlgorithm"
        private const val KEY_DOC_TYPE = "docType"
        private const val KEY_VALUE_DIGESTS = "valueDigests"
        private const val KEY_DEVICE_KEY_INFO = "deviceKeyInfo"
        private const val KEY_DEVICE_KEY = "deviceKey"
        private const val KEY_VALIDITY_INFO = "validityInfo"
        private const val KEY_STATUS = "status"
        private const val KEY_SIGNED = "signed"
        private const val KEY_VALID_FROM = "validFrom"
        private const val KEY_VALID_UNTIL = "validUntil"
        private const val KEY_EXPECTED_UPDATE = "expectedUpdate"
        private val TIMESTAMP_PATTERN = Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z""")
    }
}
