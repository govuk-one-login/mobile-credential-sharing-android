package uk.gov.onelogin.sharing.cryptoService.verifier

import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionData
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse

/**
 * [VerifierCryptoService] implementation that defers to the [updater] property for obtaining a
 * [VerifierCryptoContext].
 *
 * @param updater The lambda to call during [establishSession]. Defaults to returning `null`,
 * meaning that `updateContext` within [establishSession] isn't called.
 */
class DeferredVerifierCryptoService(
    private val updater: (qrCodeData: String) -> VerifierCryptoContext? = { null },
    private val sessionDataDeserializer: (input: ByteArray) -> SessionData = { SessionData() }
) : VerifierCryptoService {
    override fun establishSession(
        qrCodeData: String,
        updateContext: (VerifierCryptoContext) -> VerifierCryptoContext
    ) {
        updater(qrCodeData)?.let(updateContext)
    }

    override fun buildDeviceRequest(itemsRequest: ItemsRequest): ByteArray = byteArrayOf()

    override fun buildSessionEstablishment(
        eReaderKeyBytes: ByteArray,
        encryptedDeviceRequest: ByteArray
    ): ByteArray = byteArrayOf()

    override fun encryptDeviceRequest(
        deviceRequestBytes: ByteArray,
        skReader: ByteArray,
        encryptCounter: UInt
    ): ByteArray = byteArrayOf()

    override fun deserializeSessionData(input: ByteArray): SessionData =
        sessionDataDeserializer(input)

    override fun buildTerminationSessionData(): ByteArray = byteArrayOf()

    override fun decryptDeviceResponse(
        deviceResponseBytes: ByteArray,
        skDevice: ByteArray,
        decryptCounter: UInt
    ): DeviceResponse = DeviceResponse()
}
