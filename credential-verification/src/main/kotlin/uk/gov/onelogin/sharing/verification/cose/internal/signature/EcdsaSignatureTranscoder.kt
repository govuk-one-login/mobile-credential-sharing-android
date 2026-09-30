package uk.gov.onelogin.sharing.verification.cose.internal.signature

import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.InvalidSignature
import uk.gov.onelogin.sharing.verification.cose.internal.decode.CoseAlgorithm

internal object EcdsaSignatureTranscoder {
    private const val DER_SEQUENCE_TAG: Byte = 0x30
    private const val DER_INTEGER_TAG: Byte = 0x02
    private const val SIGN_BIT_MASK = 0x80

    fun rawToDer(rawSignature: ByteArray, algorithm: CoseAlgorithm? = null): ByteArray {
        if (algorithm != null && rawSignature.size != algorithm.signatureLength) {
            throw InvalidSignature
        }
        val componentLength = when (rawSignature.size) {
            CoseAlgorithm.ES256.signatureLength -> CoseAlgorithm.ES256.signatureLength / 2
            CoseAlgorithm.ES384.signatureLength -> CoseAlgorithm.ES384.signatureLength / 2
            else -> throw InvalidSignature
        }
        val r = rawSignature.copyOfRange(0, componentLength).trimLeadingZeros()
        val s = rawSignature.copyOfRange(
            componentLength,
            rawSignature.size
        ).trimLeadingZeros()
        val rEncoded = derInteger(r)
        val sEncoded = derInteger(s)
        val sequenceContent = rEncoded + sEncoded
        return byteArrayOf(DER_SEQUENCE_TAG, sequenceContent.size.toByte()) + sequenceContent
    }

    private fun derInteger(value: ByteArray): ByteArray {
        val padded = if (value[0].toInt() and SIGN_BIT_MASK !=
            0
        ) {
            byteArrayOf(0x00) + value
        } else {
            value
        }
        return byteArrayOf(DER_INTEGER_TAG, padded.size.toByte()) + padded
    }

    private fun ByteArray.trimLeadingZeros(): ByteArray {
        val firstNonZero = indexOfFirst { it.toInt() != 0 }
        return if (firstNonZero < 0) byteArrayOf(0) else copyOfRange(firstNonZero, size)
    }
}
