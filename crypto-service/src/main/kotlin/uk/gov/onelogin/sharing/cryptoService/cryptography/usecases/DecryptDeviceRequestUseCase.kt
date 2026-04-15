package uk.gov.onelogin.sharing.cryptoService.cryptography.usecases

import java.security.PrivateKey
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest

data class DecryptResult(val deviceRequest: DeviceRequest, val skDevice: ByteArray)

fun interface DecryptDeviceRequestUseCase {
    fun execute(
        sessionEstablishmentBytes: ByteArray,
        engagement: String,
        holderPrivateKey: PrivateKey,
        decryptCounter: UInt
    ): DecryptResult
}
