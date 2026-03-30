package uk.gov.onelogin.sharing.cryptoService.verifier

class FakeVerifierCryptoService : VerifierCryptoService {
    var processEngagementCallCount = 0
        private set
    var lastQrCodeData: String? = null
        private set

    override fun processEngagement(qrCodeData: String) {
        processEngagementCallCount++
        lastQrCodeData = qrCodeData
    }
}
