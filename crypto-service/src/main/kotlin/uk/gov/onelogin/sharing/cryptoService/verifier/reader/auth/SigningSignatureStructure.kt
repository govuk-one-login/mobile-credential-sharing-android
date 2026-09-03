package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import java.security.PrivateKey
import java.security.Signature
import java.security.cert.Certificate
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag

class SigningSignatureStructure(
    private val logger: Logger,
    private val privateKey: PrivateKey,
    private val signature: Signature,
    private val decorated: SigStructureGenerator,
) : SigStructureGenerator {

    override fun generateSignatureStructure(
        certificateChain: List<Certificate>,
        readerAuthenticationPayload: ByteArray,
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
            val rLen = der[offset + 1].toInt() and 0xFF
            offset += 2
            val r = der.copyOfRange(offset, offset + rLen)
            offset += rLen
            val sLen = der[offset + 1].toInt() and 0xFF
            offset += 2
            val s = der.copyOfRange(offset, offset + sLen)

            return padOrTrim(r) + padOrTrim(s)
        }

        private fun padOrTrim(bytes: ByteArray): ByteArray = when {
            bytes.size == P256_COMPONENT_SIZE + 1 && bytes[0] == 0.toByte() ->
                bytes.copyOfRange(1, bytes.size)

            bytes.size < P256_COMPONENT_SIZE -> ByteArray(P256_COMPONENT_SIZE - bytes.size) + bytes

            else -> bytes.copyOfRange(bytes.size - P256_COMPONENT_SIZE, bytes.size)
        }

        private const val P256_COMPONENT_SIZE = 32
    }
}
