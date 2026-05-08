package uk.gov.onelogin.sharing.cryptoService.verifier

import java.security.interfaces.ECPublicKey
import java.util.UUID
import uk.gov.onelogin.sharing.cryptoService.secureArea.keypair.KeyPairGeneratorStubs.validKeyPair

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
        DeferredVerifierCryptoService { qrCodeData ->
            VerifierCryptoContext(
                engagementString = qrCodeData,
                serviceUuid = UUID.randomUUID(),
                eReaderKeyTagged = byteArrayOf(),
                sessionTranscriptBytes = byteArrayOf(),
                eReaderKeyPair = validKeyPair!!,
                eDevicePublicKey = validKeyPair.public as ECPublicKey,
                skReader = sessionKeysToReturn.first,
                skDevice = sessionKeysToReturn.second
            )
        }.establishSession(qrCodeData, updateContext)
    }
}
