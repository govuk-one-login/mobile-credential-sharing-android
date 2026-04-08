package uk.gov.onelogin.sharing.cryptoService.verifier

class FakeVerifierCryptoService : VerifierCryptoService {
    var processEngagementCallCount = 0
        private set
    var lastQrCodeData: String? = null
        private set
    var deriveSessionKeysCallCount = 0
        private set
    var exceptionToThrow: Exception? = null
    var sessionKeysToReturn: Pair<ByteArray, ByteArray> =
        Pair(ByteArray(32), ByteArray(32))

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

    override fun deriveSessionKeys(
        sharedSecret: ByteArray,
        sessionTranscriptBytes: ByteArray
    ): Pair<ByteArray, ByteArray> {
        deriveSessionKeysCallCount++
        exceptionToThrow?.let { throw it }
        return sessionKeysToReturn
    }
}
