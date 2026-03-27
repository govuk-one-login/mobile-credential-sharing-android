package uk.gov.onelogin.sharing.cryptoService.verifier

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag

/**
 * Default implementation of [VerifierCryptoService].
 */
@ContributesBinding(AppScope::class, binding = binding<VerifierCryptoService>())
class VerifierCryptoServiceImpl(private val logger: Logger) : VerifierCryptoService {

    override fun processEngagement(qrCodeData: String) {
        logger.debug(logTag, "processEngagement called — not yet implemented")
    }
}
