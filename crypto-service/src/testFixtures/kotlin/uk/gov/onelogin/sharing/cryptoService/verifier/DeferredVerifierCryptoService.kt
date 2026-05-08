package uk.gov.onelogin.sharing.cryptoService.verifier

/**
 * [VerifierCryptoService] implementation that defers to the [updater] property for obtaining a
 * [VerifierCryptoContext].
 *
 * @param updater The lambda to call during [establishSession]. Defaults to returning `null`,
 * meaning that `updateContext` within [establishSession] isn't called.
 */
class DeferredVerifierCryptoService(
    private val updater: (qrCodeData: String) -> VerifierCryptoContext? = { null }
) : VerifierCryptoService {
    override fun establishSession(
        qrCodeData: String,
        updateContext: (VerifierCryptoContext) -> VerifierCryptoContext
    ) {
        updater(qrCodeData)?.let(updateContext)
    }
}
