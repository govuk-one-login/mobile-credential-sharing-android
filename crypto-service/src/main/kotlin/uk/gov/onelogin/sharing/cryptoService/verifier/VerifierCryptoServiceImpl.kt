package uk.gov.onelogin.sharing.cryptoService.verifier

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cbor.deriveSessionTranscript
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor

/**
 * Default implementation of [VerifierCryptoService].
 */
@ContributesBinding(AppScope::class, binding = binding<VerifierCryptoService>())
class VerifierCryptoServiceImpl(private val logger: Logger) : VerifierCryptoService {

    override fun processEngagement(qrCodeData: String, eReaderKeyTagged: ByteArray): ByteArray {
        require(qrCodeData.isNotBlank()) {
            logger.error(
                logTag,
                "error constructing SessionTranscript array due to DeviceEngagementBytes is blank"
            )
            "DeviceEngagementBytes must not be blank"
        }

        val sessionTranscript = deriveSessionTranscript(
            cborBase64Url = qrCodeData,
            eReaderKeyTagged = eReaderKeyTagged,
            logger = logger
        )

        val sessionTranscriptBytes = EmbeddedCbor(sessionTranscript).encodeCbor()

        logger.debug(logTag, "SessionTranscriptBytes constructed successfully")

        return sessionTranscriptBytes
    }
}
