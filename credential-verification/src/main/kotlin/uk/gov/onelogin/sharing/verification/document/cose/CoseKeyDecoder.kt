package uk.gov.onelogin.sharing.verification.document.cose

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import dev.zacsweers.metro.Inject
import java.security.KeyFactory
import java.security.interfaces.ECPublicKey
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import java.security.spec.EllipticCurve
import java.security.spec.ECFieldFp
import java.security.spec.ECParameterSpec
import java.math.BigInteger
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult

@Inject
internal class CoseKeyDecoder {
    private val cborMapper = ObjectMapper(CBORFactory())

    companion object {
        private const val LABEL_KTY = 1L
        private const val LABEL_CRV = -1L
        private const val LABEL_X = -2L
        private const val LABEL_Y = -3L
        private const val KTY_EC2 = 2L
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
        val node = try {
            cborMapper.readTree(bytes)
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_KEY)
        }
        val result = mutableMapOf<Long, Any>()
        for (entry in node.properties()) {
            val key = entry.key.toLongOrNull()
                ?: throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_KEY)
            result[key] = when {
                entry.value.isIntegralNumber -> entry.value.longValue()
                entry.value.isBinary -> entry.value.binaryValue()
                else -> throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_KEY)
            }
        }
        return result
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

    private fun extractCoordinate(map: Map<Long, Any>, label: Long): ByteArray {
        return map[label] as? ByteArray
            ?: throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_KEY)
    }

    private fun buildECPublicKey(x: ByteArray, y: ByteArray): ECPublicKey {
        return try {
            val point = ECPoint(BigInteger(1, x), BigInteger(1, y))
            val keyFactory = KeyFactory.getInstance("EC")
            val spec = ECPublicKeySpec(point, ecP256ParameterSpec())
            keyFactory.generatePublic(spec) as ECPublicKey
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_KEY)
        }
    }

    private fun ecP256ParameterSpec(): ECParameterSpec {
        val p = BigInteger(
            "FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16
        )
        val a = BigInteger(
            "FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC", 16
        )
        val b = BigInteger(
            "5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 16
        )
        val gx = BigInteger(
            "6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", 16
        )
        val gy = BigInteger(
            "4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5", 16
        )
        val n = BigInteger(
            "FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16
        )
        val curve = EllipticCurve(ECFieldFp(p), a, b)
        val generator = ECPoint(gx, gy)
        return ECParameterSpec(curve, generator, n, 1)
    }
}
