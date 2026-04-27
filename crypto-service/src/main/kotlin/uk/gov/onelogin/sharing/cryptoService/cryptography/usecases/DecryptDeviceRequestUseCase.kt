package uk.gov.onelogin.sharing.cryptoService.cryptography.usecases

import java.security.PrivateKey
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest

@Suppress("LongParameterList")
fun interface DecryptDeviceRequestUseCase {
    fun execute(
        sessionEstablishmentBytes: ByteArray,
        engagement: String,
        holderPrivateKey: PrivateKey,
        decryptCounter: UInt,
        onDeriveSkDevice: (ByteArray) -> Unit,
        onDeriveSessionTranscript: (ByteArray) -> Unit
    ): DeviceRequest
}
