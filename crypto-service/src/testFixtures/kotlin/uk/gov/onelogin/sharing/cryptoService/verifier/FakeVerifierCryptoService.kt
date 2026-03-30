package uk.gov.onelogin.sharing.cryptoService.verifier

class FakeVerifierCryptoService : VerifierCryptoService {
    var processEngagementCallCount = 0
        private set
    var lastQrCodeData: String? = null
        private set
    var resultToReturn = ProcessEngagementResult(byteArrayOf(), byteArrayOf())
    var exceptionToThrow: Exception? = null

    override fun processEngagement(qrCodeData: String): ProcessEngagementResult {
        processEngagementCallCount++
        lastQrCodeData = qrCodeData
        exceptionToThrow?.let { throw it }
        return resultToReturn
    }
}
