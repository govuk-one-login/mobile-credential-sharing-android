package uk.gov.onelogin.sharing.verification

import uk.gov.onelogin.sharing.models.mdoc.transcript.SessionTranscript

fun interface DocumentVerifier {
    /**
     * @throws VerificationResult.Failure when unable to successfully verify the provided
     * [document].
     *
     * @param document The [VerifiableDocument] to verify.
     * @param transcript Provides additional information for use in verifying
     * [VerifiableDocument.WithPresentation] instances.
     */
    fun verifyDocument(
        document: VerifiableDocument,
        transcript: SessionTranscript?
    ): VerificationResult.Success
}
