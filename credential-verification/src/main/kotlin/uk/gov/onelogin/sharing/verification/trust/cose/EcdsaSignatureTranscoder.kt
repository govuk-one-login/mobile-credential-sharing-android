package uk.gov.onelogin.sharing.verification.trust.cose

import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult

internal object EcdsaSignatureTranscoder {
    private const val ES256_SIGNATURE_LENGTH = 64
    private const val COMPONENT_LENGTH = 32
    private const val DER_SEQUENCE_TAG: Byte = 0x30
    private const val DER_INTEGER_TAG: Byte = 0x02
    private const val SIGN_BIT_MASK = 0x80

    fun rawToDer(rawSignature: ByteArray, error: VerificationError): ByteArray {
        if (rawSignature.size != ES256_SIGNATURE_LENGTH) {
            throw VerificationResult.Failure(error)
        }
        val r = rawSignature.copyOfRange(0, COMPONENT_LENGTH).trimLeadingZeros()
        val s = rawSignature.copyOfRange(
            COMPONENT_LENGTH,
            ES256_SIGNATURE_LENGTH
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
