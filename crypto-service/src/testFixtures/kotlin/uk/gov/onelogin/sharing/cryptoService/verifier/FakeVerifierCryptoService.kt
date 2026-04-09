package uk.gov.onelogin.sharing.cryptoService.verifier

class FakeVerifierCryptoService : VerifierCryptoService {
    var establishSessionCallCount = 0
        private set
    var lastQrCodeData: String? = null
        private set
    var exceptionToThrow: Exception? = null
    var sessionKeysToReturn: Pair<ByteArray, ByteArray> =
        Pair(ByteArray(32), ByteArray(32))

    override fun establishSession(
        qrCodeData: String,
        updateContext: (VerifierCryptoContext) -> VerifierCryptoContext
    ) {
        establishSessionCallCount++
        lastQrCodeData = qrCodeData
        exceptionToThrow?.let { throw it }
        updateContext(
            VerifierCryptoContext(
                engagementString = qrCodeData,
                serviceUuid = java.util.UUID.randomUUID(),
                skReader = sessionKeysToReturn.first,
                skDevice = sessionKeysToReturn.second
            )
        )
    }
}
