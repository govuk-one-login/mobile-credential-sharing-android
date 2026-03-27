package uk.gov.onelogin.sharing.cryptoService.verifier

class FakeVerifierCryptoService : VerifierCryptoService {
    var processEngagementCallCount = 0
        private set
    var lastQrCodeData: String? = null
        private set
    var lastEReaderKeyTagged: ByteArray? = null
        private set
    var resultToReturn: ByteArray = byteArrayOf()
    var exceptionToThrow: Exception? = null

    override fun processEngagement(qrCodeData: String, eReaderKeyTagged: ByteArray): ByteArray {
        processEngagementCallCount++
        lastQrCodeData = qrCodeData
        lastEReaderKeyTagged = eReaderKeyTagged
        exceptionToThrow?.let { throw it }
        return resultToReturn
    }
}
