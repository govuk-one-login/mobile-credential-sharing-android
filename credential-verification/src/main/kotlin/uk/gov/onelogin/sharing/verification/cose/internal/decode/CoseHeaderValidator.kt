package uk.gov.onelogin.sharing.verification.cose.internal.decode

import com.fasterxml.jackson.core.StreamReadFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import dev.zacsweers.metro.Inject
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.MalformedCoseSign1
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.UnsupportedAlgorithm

@Inject
internal class CoseHeaderValidator {

    private val cborMapper: ObjectMapper = JsonMapper.builder(CBORFactory())
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
        .build()

    companion object {
        private const val COSE_ALG_LABEL = 1L
        private const val COSE_ALG_ES256 = -7L
    }

    fun validate(coseSign1: InternalCoseSign1) {
        val protectedMap = decodeHeaderMap(coseSign1.protectedHeader)
        val unprotectedMap =
            coseSign1.unprotectedHeader?.let { decodeHeaderMap(it) } ?: emptyMap()

        val isValid = COSE_ALG_LABEL in protectedMap &&
            COSE_ALG_LABEL !in unprotectedMap &&
            protectedMap.keys.none { it in unprotectedMap } &&
            protectedMap[COSE_ALG_LABEL] == COSE_ALG_ES256

        if (!isValid) throw UnsupportedAlgorithm
    }

    private fun decodeHeaderMap(headerBytes: ByteArray): Map<Long, Any> {
        val node = try {
            cborMapper.readTree(headerBytes)
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            null
        } ?: throw MalformedCoseSign1

        val result = mutableMapOf<Long, Any>()
        for (entry in node.properties()) {
            val key = entry.key.toLongOrNull()
            if (key == null || key in result) throw MalformedCoseSign1
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
