package uk.gov.onelogin.sharing.security.cryptography.usecases

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import java.security.PrivateKey
import java.security.interfaces.ECPrivateKey
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.security.cbor.decodeSessionEstablishmentModel
import uk.gov.onelogin.sharing.security.cbor.decoders.DeviceRequestDecoder
import uk.gov.onelogin.sharing.security.cbor.deriveSessionTranscript
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity
import uk.gov.onelogin.sharing.security.secureArea.session.SessionKeyGenerator
import uk.gov.onelogin.sharing.security.toSessionEstablishment

@ContributesBinding(scope = AppScope::class, binding = binding<DecryptDeviceRequestUseCase>())
class DecryptDeviceRequestUseCaseImpl(
    private val sessionSecurity: SessionSecurity,
    private val deviceRequestDecoder: DeviceRequestDecoder,
    private val logger: Logger
) : DecryptDeviceRequestUseCase {
    override fun execute(
        sessionEstablishmentBytes: ByteArray,
        engagement: String,
        holderPrivateKey: PrivateKey
    ): DeviceRequest {
        val sessionEstablishment = decodeSessionEstablishmentModel(
            rawBytes = sessionEstablishmentBytes,
            logger = logger
        ).toSessionEstablishment()

        val eReaderPublicKey = CoseKey.getEReaderKeyFromParsedCoseKey(
            sessionEstablishment.eReaderKey
        )

        val sharedSecret = sessionSecurity.generateSharedSecret(
            holderKey = holderPrivateKey as ECPrivateKey,
            eReaderKey = eReaderPublicKey
        )

        val transcript = deriveSessionTranscript(
            cborBase64Url = engagement,
            eReaderKeyTagged = sessionEstablishment.eReaderKey,
            logger = logger
        )

        val skReader = sessionSecurity.deriveSessionKey(
            sharedKey = sharedSecret,
            sessionTranscriptBytes = transcript,
            role = SessionKeyGenerator.Companion.DeviceRole.VERIFIER
        )

        val plaintext = sessionSecurity.decryptPayload(
            key = skReader,
            data = sessionEstablishment.data,
            role = SessionKeyGenerator.Companion.DeviceRole.VERIFIER
        )

        return deviceRequestDecoder.deviceRequestDecoder(plaintext)
    }
}
