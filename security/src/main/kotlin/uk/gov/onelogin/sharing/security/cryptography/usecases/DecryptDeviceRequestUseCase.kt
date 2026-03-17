package uk.gov.onelogin.sharing.security.cryptography.usecases

import java.security.PrivateKey
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest

fun interface DecryptDeviceRequestUseCase {
    fun execute(
        sessionEstablishmentBytes: ByteArray,
        engagement: String,
        holderPrivateKey: PrivateKey,
        decryptCounter: UInt
    ): DeviceRequest
}
