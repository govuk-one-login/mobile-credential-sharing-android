package uk.gov.onelogin.sharing.orchestration.holder.session

import kotlinx.coroutines.CompletableDeferred
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingDeviceSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingIssuerSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingVerifiableDocumentWithPresentation
import uk.gov.onelogin.sharing.orchestration.holder.credential.ValidatedCredential
import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument

class FakeConfirmConsentUseCase(
    private val exception: Exception? = null,
    private val documentToReturn: VerifiableDocument.WithPresentation =
        SharingVerifiableDocumentWithPresentation(
            docType = "",
            issuerSigned = SharingIssuerSigned(
                nameSpaces = emptyMap(),
                issuerAuth = byteArrayOf()
            ),
            deviceSigned = SharingDeviceSigned(
                deviceNameSpacesBytes = byteArrayOf(),
                deviceSignature = byteArrayOf()
            )
        ),
    private val gate: CompletableDeferred<Unit>? = null,
    /**
     * When set, [exception] is thrown only for the first [failuresBeforeSuccess] [execute] calls;
     * subsequent calls succeed and return [documentToReturn]. This models a signing failure that
     * can be retried (e.g. a recoverable local-authentication cancellation).
     *
     * When `null` (default) the original behaviour applies: if [exception] is set it is thrown on
     * every call.
     */
    private val failuresBeforeSuccess: Int? = null
) : ConfirmConsentUseCase {

    private var attempts = 0

    override suspend fun execute(
        sessionTranscript: ByteArray,
        deviceRequest: DeviceRequest,
        validatedCredential: ValidatedCredential,
        filteredIssuerSigned: IssuerSigned
    ): VerifiableDocument.WithPresentation {
        gate?.await()
        attempts++
        exception?.let {
            val shouldThrow = failuresBeforeSuccess == null || attempts <= failuresBeforeSuccess
            if (shouldThrow) throw it
        }
        return documentToReturn
    }
}
