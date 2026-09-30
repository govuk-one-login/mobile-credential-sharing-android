package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import java.security.PrivateKey
import java.security.Signature
import java.security.cert.Certificate
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag

/**
 * [SigStructureGenerator] implementation that acts as a decorator to the [decorated]
 * implementation. Doing so separates the CBOR-encoding logic from the signing logic whilst
 * maintaining the same function contract.
 *
 * Uses the [privateKey] to begin the [signature]'s signing process on [decorated]'s return value
 * to obtain a `DER` encoded ECDSA signature. The `DER` signature is then converted to a raw format
 * (`R || S`) for use within COSE related data transfer.
 *
 * @property logger The GOV.UK [Logger] implementation to send status updates to.
 * @property privateKey The [PrivateKey] to register the [signature] with.
 * @property signature The [Signature] to perform signing operations with.
 * @property decorated The [SigStructureGenerator] implementation providing the [ByteArray] data to
 * sign.
 */
class SigningSignatureStructure(
    private val logger: Logger,
    private val privateKey: PrivateKey,
    private val signature: Signature,
    private val decorated: SigStructureGenerator
) : SigStructureGenerator {

    override fun generateSignatureStructure(
        certificateChain: List<Certificate>,
        readerAuthenticationPayload: ByteArray
    ): ByteArray = decorated.generateSignatureStructure(
        certificateChain,
        readerAuthenticationPayload
    ).let { unsignedBytes ->
        signature.run {
            initSign(privateKey)
            update(unsignedBytes)
            sign()
        }
    }.let(::derToRaw)
        .also {
            logger.debug(
                logTag,
                "Successfully signed CBOR-encoded Sig_Structure array: ${it.toHexString()}"
            )
        }

    companion object {
        /**
         * Converts DER-encoded ECDSA signature to raw (r || s) format expected by COSE.
         */
        fun derToRaw(der: ByteArray): ByteArray {
            var offset = 2
            offset++
            val rLen = der[offset].toInt() and BYTE_MASK
            offset++
            val r = der.copyOfRange(offset, offset + rLen)
            offset += rLen
            offset++
            val sLen = der[offset].toInt() and BYTE_MASK
            offset++
            val s = der.copyOfRange(offset, offset + sLen)

            val unpaddedRLen = if (r.isNotEmpty() && r[0] == 0.toByte()) r.size - 1 else r.size
            val unpaddedSLen = if (s.isNotEmpty() && s[0] == 0.toByte()) s.size - 1 else s.size
            val componentSize = if (unpaddedRLen > P256_COMPONENT_SIZE ||
                unpaddedSLen > P256_COMPONENT_SIZE
            ) {
                P384_COMPONENT_SIZE
            } else {
                P256_COMPONENT_SIZE
            }

            return padOrTrim(r, componentSize) + padOrTrim(s, componentSize)
        }

        private fun padOrTrim(bytes: ByteArray, componentSize: Int): ByteArray = when {
            bytes.size == componentSize + 1 && bytes[0] == 0.toByte() ->
                bytes.copyOfRange(1, bytes.size)

            bytes.size < componentSize ->
                ByteArray(componentSize - bytes.size) + bytes

            else ->
                bytes.copyOfRange(bytes.size - componentSize, bytes.size)
        }

        private const val P256_COMPONENT_SIZE = 32
        private const val P384_COMPONENT_SIZE = 48
        private const val BYTE_MASK = 0xFF
    }
}
