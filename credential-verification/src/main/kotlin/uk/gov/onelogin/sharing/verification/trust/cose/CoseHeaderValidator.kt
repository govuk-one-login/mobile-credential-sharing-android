package uk.gov.onelogin.sharing.verification.trust.cose

import com.fasterxml.jackson.core.StreamReadFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import dev.zacsweers.metro.Inject
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.verification.format.cose.CoseSign1
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult

@Inject
internal class CoseHeaderValidator(private val logger: Logger) {
    private val logTag = this::class.java.simpleName
    private val cborMapper: ObjectMapper = JsonMapper.builder(CBORFactory())
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
        .build()

    companion object {
        private const val COSE_ALG_LABEL = 1L
        private const val COSE_ALG_ES256 = -7L
    }

    fun validate(coseSign1: CoseSign1, error: VerificationError) {
        val protectedMap = decodeHeaderMap(coseSign1.protectedHeader, error)
        val unprotectedMap =
            coseSign1.unprotectedHeader?.let { decodeHeaderMap(it, error) } ?: emptyMap()

        val isValid = COSE_ALG_LABEL in protectedMap &&
            COSE_ALG_LABEL !in unprotectedMap &&
            protectedMap.keys.none { it in unprotectedMap } &&
            protectedMap[COSE_ALG_LABEL] == COSE_ALG_ES256

        if (!isValid) throw VerificationResult.Failure(error)
    }

    private fun decodeHeaderMap(headerBytes: ByteArray, error: VerificationError): Map<Long, Any> {
        val node = try {
            cborMapper.readTree(headerBytes)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.error(logTag, "Failed to decode COSE header", e)
            throw VerificationResult.Failure(error)
        }
        val result = mutableMapOf<Long, Any>()
        for (entry in node.properties()) {
            val key = entry.key.toLongOrNull()
            if (key == null || key in result) throw VerificationResult.Failure(error)
            result[key] = when {
                entry.value.isIntegralNumber -> entry.value.longValue()
                entry.value.isBinary -> entry.value.binaryValue()
                entry.value.isTextual -> entry.value.textValue()
                else -> entry.value
            }
        }
        return result
    }
}
