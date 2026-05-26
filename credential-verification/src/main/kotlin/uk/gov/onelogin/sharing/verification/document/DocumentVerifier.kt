package uk.gov.onelogin.sharing.verification.document

import uk.gov.onelogin.sharing.models.mdoc.transcript.SessionTranscript
import uk.gov.onelogin.sharing.verification.document.DocumentVerifier.Companion.exampleVerifierUsage
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult

fun interface DocumentVerifier {
    /**
     * @param document The [uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument] to verify.
     * @param transcript Provides additional information for use in verifying
     * [uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument.WithPresentation] instances.
     * @throws VerificationResult.Failure when unable to successfully verify the provided
     * [document].
     * @sample exampleVerifierUsage
     */
    fun verifyDocument(
        document: VerifiableDocument,
        transcript: SessionTranscript?
    ): VerificationResult.Success

    companion object {
        internal fun exampleVerifierUsage(
            verifier: DocumentVerifier,
            document: VerifiableDocument,
            transcript: SessionTranscript? = null,
            handleVerificationFailure: (VerificationError) -> Unit,
            handleJourneyCompletion: () -> Unit
        ) {
            runCatching {
                verifier.verifyDocument(document, transcript)
            }.onFailure { throwable ->
                when (throwable) {
                    is VerificationResult.Failure -> handleVerificationFailure(throwable.error)
                    else -> throw throwable
                }
            }.onSuccess { _: VerificationResult.Success ->
                handleJourneyCompletion()
            }
        }
    }
}
