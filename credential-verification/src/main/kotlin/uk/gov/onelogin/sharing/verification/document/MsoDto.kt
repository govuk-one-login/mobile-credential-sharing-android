package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObject
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceKeyInfo
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.validity.ValidityInfo

@OptIn(ExperimentalTime::class)
@JsonDeserialize(using = MsoDto.MsoDtoDeserializer::class)
internal data class MsoDto(
    val version: String,
    val digestAlgorithm: String,
    val docType: String,
    val valueDigests: Map<String, Map<Int, ByteArray>>,
    val deviceKeyInfo: ByteArray,
    val validityInfo: ValidityInfoDto,
    val status: ByteArray? = null
) {
    fun toDomain(): MobileSecurityObject = MobileSecurityObject(
        version = version,
        digestAlgorithm = digestAlgorithm,
        docType = docType,
        valueDigests = valueDigests,
        deviceKeyInfo = DeviceKeyInfo(deviceKey = deviceKeyInfo),
        validityInfo = ValidityInfo(
            signed = validityInfo.signed,
            validFrom = validityInfo.validFrom,
            validUntil = validityInfo.validUntil,
            expectedUpdate = validityInfo.expectedUpdate
        ),
        status = status
    )

    data class ValidityInfoDto(
        val signed: Instant,
        val validFrom: Instant,
        val validUntil: Instant,
        val expectedUpdate: Instant? = null
    )

    class MsoDtoDeserializer : StdDeserializer<MsoDto>(MsoDto::class.java) {
        private val cborMapper = ObjectMapper(CBORFactory())

        override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): MsoDto {
            val root = cborMapper.readTree<JsonNode>(parser)

            val version = root.requireText(KEY_VERSION)
            val digestAlgorithm = root.requireText(KEY_DIGEST_ALGORITHM)
            val docType = root.requireText(KEY_DOC_TYPE)
            val valueDigests = deserializeValueDigests(root.require(KEY_VALUE_DIGESTS))
            val deviceKeyInfo = deserializeDeviceKeyInfo(root.require(KEY_DEVICE_KEY_INFO))
            val validityInfo = deserializeValidityInfo(root.require(KEY_VALIDITY_INFO))
            val status = (root.get(KEY_STATUS) as? BinaryNode)?.binaryValue()

            return MsoDto(
                version = version,
                digestAlgorithm = digestAlgorithm,
                docType = docType,
                valueDigests = valueDigests,
                deviceKeyInfo = deviceKeyInfo,
                validityInfo = validityInfo,
                status = status
            )
        }

        private fun deserializeValueDigests(node: JsonNode): Map<String, Map<Int, ByteArray>> {
            val result = mutableMapOf<String, Map<Int, ByteArray>>()
            node.properties().forEach { (namespace, digestsNode) ->
                val digests = mutableMapOf<Int, ByteArray>()
                digestsNode.properties().forEach { (id, value) ->
                    val digestId = id.toIntOrNull() ?: throw malformed
                    digests[digestId] =
                        (value as? BinaryNode)?.binaryValue() ?: throw malformed
                }
                result[namespace] = digests
            }
            return result
        }

        private fun deserializeDeviceKeyInfo(node: JsonNode): ByteArray {
            val deviceKeyNode = node.get(KEY_DEVICE_KEY) ?: throw malformed
            return cborMapper.writeValueAsBytes(deviceKeyNode)
        }

        private fun deserializeValidityInfo(node: JsonNode): ValidityInfoDto {
            val signed = node.requireTimestamp(KEY_SIGNED)
            val validFrom = node.requireTimestamp(KEY_VALID_FROM)
            val validUntil = node.requireTimestamp(KEY_VALID_UNTIL)
            val expectedUpdate = node.get(KEY_EXPECTED_UPDATE)?.asText()
                ?.also { if (!TIMESTAMP_PATTERN.matches(it)) throw malformed }
                ?.let { Instant.parse(it) }

            return ValidityInfoDto(
                signed = signed,
                validFrom = validFrom,
                validUntil = validUntil,
                expectedUpdate = expectedUpdate
            )
        }

        private fun JsonNode.require(key: String): JsonNode = get(key) ?: throw malformed

        private fun JsonNode.requireText(key: String): String =
            get(key)?.asText() ?: throw malformed

        private fun JsonNode.requireTimestamp(key: String): Instant {
            val value = get(key)?.asText() ?: throw malformed
            if (!TIMESTAMP_PATTERN.matches(value)) throw malformed
            return Instant.parse(value)
        }

        private val malformed
            get() = VerificationResult.Failure(VerificationError.MALFORMED_MSO)

        private companion object {
            const val KEY_VERSION = "version"
            const val KEY_DIGEST_ALGORITHM = "digestAlgorithm"
            const val KEY_DOC_TYPE = "docType"
            const val KEY_VALUE_DIGESTS = "valueDigests"
            const val KEY_DEVICE_KEY_INFO = "deviceKeyInfo"
            const val KEY_DEVICE_KEY = "deviceKey"
            const val KEY_VALIDITY_INFO = "validityInfo"
            const val KEY_STATUS = "status"
            const val KEY_SIGNED = "signed"
            const val KEY_VALID_FROM = "validFrom"
            const val KEY_VALID_UNTIL = "validUntil"
            const val KEY_EXPECTED_UPDATE = "expectedUpdate"
            val TIMESTAMP_PATTERN = Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z""")
        }
    }
}
