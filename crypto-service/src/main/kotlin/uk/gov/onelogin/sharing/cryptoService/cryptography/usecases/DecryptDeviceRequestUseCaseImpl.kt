package uk.gov.onelogin.sharing.cryptoService.cryptography.usecases

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import java.security.PrivateKey
import java.security.interfaces.ECPrivateKey
import kotlin.collections.component1
import kotlin.collections.component2
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cbor.decodeSessionEstablishmentModel
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestDecoder
import uk.gov.onelogin.sharing.cryptoService.cbor.deriveSessionTranscript
import uk.gov.onelogin.sharing.cryptoService.cose.CoseKey
import uk.gov.onelogin.sharing.cryptoService.secureArea.SessionSecurity
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator
import uk.gov.onelogin.sharing.cryptoService.toSessionEstablishment
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest

@ContributesBinding(scope = AppScope::class, binding = binding<DecryptDeviceRequestUseCase>())
class DecryptDeviceRequestUseCaseImpl(
    private val sessionSecurity: SessionSecurity,
    private val deviceRequestDecoder: DeviceRequestDecoder,
    private val logger: Logger
) : DecryptDeviceRequestUseCase {
    override fun execute(
        sessionEstablishmentBytes: ByteArray,
        engagement: String,
        holderPrivateKey: PrivateKey,
        decryptCounter: UInt,
        onDeriveSkDevice: (ByteArray) -> Unit,
        onDeriveSessionTranscript: (ByteArray) -> Unit
    ): DeviceRequest {
        val sessionEstablishment = decodeSessionEstablishmentModel(
            rawBytes = sessionEstablishmentBytes,
            logger = logger
        ).toSessionEstablishment()

        val eReaderPublicKey = CoseKey.getEReaderKeyFromParsedCoseKey(
            sessionEstablishment.eReaderKey
        )

        val sharedSecret = sessionSecurity.generateSharedSecret(
            thisDevicePrivateKey = holderPrivateKey as ECPrivateKey,
            otherDevicePublicKey = eReaderPublicKey
        )

        val transcript = deriveSessionTranscript(
            cborBase64Url = engagement,
            eReaderKeyTagged = sessionEstablishment.eReaderKey,
            logger = logger
        )

        onDeriveSessionTranscript(transcript)

        val skReader = sessionSecurity.deriveSessionKey(
            sharedKey = sharedSecret,
            sessionTranscriptBytes = transcript,
            role = SessionKeyGenerator.Companion.DeviceRole.VERIFIER
        )

        val skDevice = sessionSecurity.deriveSessionKey(
            sharedKey = sharedSecret,
            sessionTranscriptBytes = transcript,
            role = SessionKeyGenerator.Companion.DeviceRole.HOLDER
        )

        onDeriveSkDevice(skDevice)

        val plaintext = sessionSecurity.decryptPayload(
            key = skReader,
            data = sessionEstablishment.data,
            role = SessionKeyGenerator.Companion.DeviceRole.VERIFIER,
            decryptCounter = decryptCounter
        )

        val deviceRequest = deviceRequestDecoder.deviceRequestDecoder(plaintext)

        deviceRequest
            .docRequests.firstOrNull()
            ?.itemsRequest
            ?.nameSpaces
            ?.forEach { (key, value) ->
                logger.debug(logTag, "Requests: key = $key, value = $value")
            }

        return deviceRequest
    }
}
