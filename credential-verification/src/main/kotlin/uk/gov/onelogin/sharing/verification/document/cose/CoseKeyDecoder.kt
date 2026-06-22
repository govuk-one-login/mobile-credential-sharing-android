package uk.gov.onelogin.sharing.verification.document.cose

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import dev.zacsweers.metro.Inject
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult

@Inject
class CoseKeyDecoder {
    private val cborMapper = ObjectMapper(CBORFactory())

    companion object {
        private const val LABEL_KTY = 1L
        private const val LABEL_CRV = -1L
        private const val LABEL_X = -2L
        private const val LABEL_Y = -3L

        /**
         * Use the `ECKeyType` enum instead
         */
        private const val KTY_EC2 = 2L

        /**
         * Use the `ECType` enum instead
         */
        private const val CRV_P256 = 1L
        private val ALLOWED_LABELS = setOf(LABEL_KTY, LABEL_CRV, LABEL_X, LABEL_Y)
    }

    fun decode(coseKeyBytes: ByteArray): ECPublicKey {
        val map = parseCoseKeyMap(coseKeyBytes)
        validateLabels(map)
        validateKeyType(map)
        val x = extractCoordinate(map, LABEL_X)
        val y = extractCoordinate(map, LABEL_Y)
        return buildECPublicKey(x, y)
    }

    private fun parseCoseKeyMap(bytes: ByteArray): Map<Long, Any> {
        try {
            val node = cborMapper.readTree(bytes)
            val result = mutableMapOf<Long, Any>()
            for (entry in node.properties()) {
                val key = entry.key.toLongOrNull()
                    ?: error("invalid key")
                result[key] = when {
                    entry.value.isIntegralNumber -> entry.value.longValue()
                    entry.value.isBinary -> entry.value.binaryValue()
                    else -> error("unsupported value")
                }
            }
            return result
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_KEY)
        }
    }

    private fun validateLabels(map: Map<Long, Any>) {
        if (map.keys != ALLOWED_LABELS) {
            throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_KEY)
        }
    }

    private fun validateKeyType(map: Map<Long, Any>) {
        if (map[LABEL_KTY] != KTY_EC2 || map[LABEL_CRV] != CRV_P256) {
            throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_KEY)
        }
    }

    private fun extractCoordinate(map: Map<Long, Any>, label: Long): ByteArray =
        map[label] as? ByteArray
            ?: throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_KEY)

    private fun buildECPublicKey(x: ByteArray, y: ByteArray): ECPublicKey = try {
        val point = ECPoint(BigInteger(1, x), BigInteger(1, y))
        val keyFactory = KeyFactory.getInstance("EC")
        val spec = ECPublicKeySpec(point, ecP256ParameterSpec())
        keyFactory.generatePublic(spec) as ECPublicKey
    } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
        throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_KEY)
    }

    private fun ecP256ParameterSpec(): ECParameterSpec {
        val params = java.security.AlgorithmParameters.getInstance("EC")
        params.init(ECGenParameterSpec("secp256r1"))
        return params.getParameterSpec(ECParameterSpec::class.java)
    }
}
