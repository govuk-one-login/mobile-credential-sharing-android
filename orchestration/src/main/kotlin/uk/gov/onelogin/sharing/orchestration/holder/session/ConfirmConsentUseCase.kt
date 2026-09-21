package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.orchestration.SignException
import uk.gov.onelogin.sharing.orchestration.holder.credential.ValidatedCredential
import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument

fun interface ConfirmConsentUseCase {
    /**
     * Builds the [VerifiableDocument.WithPresentation] to send to the Verifier, signing the
     * device authentication via the host app's `CredentialProvider.sign`.
     *
     * @throws SignException.LocalAuthCancelled when the user cancelled the local-authentication
     * prompt while signing. This is a neutral outcome; the caller keeps the session active and
     * lets the user retry.
     * @throws DeviceSignatureException when signing fails for any other reason (a fatal outcome).
     */
    @Throws(SignException.LocalAuthCancelled::class, DeviceSignatureException::class)
    suspend fun execute(
        sessionTranscript: ByteArray,
        deviceRequest: DeviceRequest,
        validatedCredential: ValidatedCredential,
        filteredIssuerSigned: IssuerSigned
    ): VerifiableDocument.WithPresentation
}
