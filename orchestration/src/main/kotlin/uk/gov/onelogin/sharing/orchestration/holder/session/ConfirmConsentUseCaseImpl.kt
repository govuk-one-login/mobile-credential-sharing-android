package uk.gov.onelogin.sharing.orchestration.holder.session

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.cryptoService.holder.HolderCryptoService
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSigned
import uk.gov.onelogin.sharing.orchestration.holder.credential.ValidatedCredential

@Inject
@ContributesBinding(scope = AppScope::class, binding = binding<ConfirmConsentUseCase>())
class ConfirmConsentUseCaseImpl(
    private val holderCryptoService: HolderCryptoService,
    private val holderResponseUseCase: HolderResponseUseCase
) : ConfirmConsentUseCase {

    override suspend fun execute(
        sessionTranscript: ByteArray,
        deviceRequest: DeviceRequest,
        validatedCredential: ValidatedCredential,
        filteredIssuerSigned: IssuerSigned
    ): DeviceSigned {
        val docType = deviceRequest.docRequests.firstOrNull()?.itemsRequest?.docType
            ?: throw DeviceSignatureException("Missing docType")

        val authResult = holderCryptoService.buildDeviceAuthenticationBytes(
            sessionTranscript = sessionTranscript,
            docType = docType
        )

        return holderResponseUseCase.generateDeviceResponse(
            validatedCredential = validatedCredential,
            deviceAuthenticationBytes = authResult.deviceAuthenticationBytes
        )
    }
}
