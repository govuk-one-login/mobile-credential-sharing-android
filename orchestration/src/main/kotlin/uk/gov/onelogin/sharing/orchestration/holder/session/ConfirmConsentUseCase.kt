package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.orchestration.CredentialSigningException
import uk.gov.onelogin.sharing.orchestration.holder.credential.ValidatedCredential
import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument

fun interface ConfirmConsentUseCase {
    /**
     * Builds the [VerifiableDocument.WithPresentation] to send to the Verifier, signing the
     * device authentication via the host app's `CredentialProvider.sign`.
     *
     * @throws CredentialSigningException.Recoverable when signing did not complete but the session
     * can continue (e.g. the user cancelled the local-authentication prompt). This is a neutral
     * outcome; the caller keeps the session active and lets the user retry.
     * @throws DeviceSignatureException when signing fails for any other reason (a fatal outcome).
     */
    @Throws(CredentialSigningException.Recoverable::class, DeviceSignatureException::class)
    suspend fun execute(
        sessionTranscript: ByteArray,
        deviceRequest: DeviceRequest,
        validatedCredential: ValidatedCredential,
        filteredIssuerSigned: IssuerSigned
    ): VerifiableDocument.WithPresentation
}
