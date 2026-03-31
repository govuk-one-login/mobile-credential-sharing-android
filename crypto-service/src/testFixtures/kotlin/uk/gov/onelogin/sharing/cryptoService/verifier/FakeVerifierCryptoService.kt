package uk.gov.onelogin.sharing.cryptoService.verifier

class FakeVerifierCryptoService : VerifierCryptoService {
    var processEngagementCallCount = 0
        private set
    var lastQrCodeData: String? = null
        private set
    var exceptionToThrow: Exception? = null

    override fun processEngagement(
        qrCodeData: String,
        updateContext: (VerifierCryptoContext) -> VerifierCryptoContext
    ) {
        processEngagementCallCount++
        lastQrCodeData = qrCodeData
        exceptionToThrow?.let { throw it }
        updateContext(
            VerifierCryptoContext(
                engagementString = qrCodeData,
                serviceUuid = java.util.UUID.randomUUID()
            )
        )
    }
}
