package uk.gov.onelogin.sharing.cryptoService.usecases

import java.security.PrivateKey
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub.deviceRequestStub
import uk.gov.onelogin.sharing.cryptoService.cryptography.usecases.DecryptDeviceRequestUseCase
import uk.gov.onelogin.sharing.cryptoService.cryptography.usecases.DecryptResult

class FakeDecryptDeviceRequestUseCase : DecryptDeviceRequestUseCase {
    var skDeviceToReturn: ByteArray = byteArrayOf(0x01, 0x02)
    var exception: Exception? = null

    override fun execute(
        sessionEstablishmentBytes: ByteArray,
        engagement: String,
        holderPrivateKey: PrivateKey,
        decryptCounter: UInt
    ): DecryptResult {
        exception?.let { throw it }
        return DecryptResult(
            deviceRequest = deviceRequestStub,
            skDevice = skDeviceToReturn
        )
    }
}
