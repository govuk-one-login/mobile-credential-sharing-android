package uk.gov.onelogin.sharing.orchestration

/**
 * Provider interface for Holder role.
 * Host app implements this to supply credentials and signatures.
 */
interface CredentialProvider {
    suspend fun getCredentials(request: CredentialRequest): List<Credential>

    /**
     * Signs the COSE `Sig_structure` [payload] with the private key bound to [documentId],
     * returning the DER-encoded ECDSA (ES256 / P-256) signature bytes.
     *
     * @deprecated Superseded by [signV2], which returns a [SignResult] so recoverable failures
     * (for example the user cancelling the local-authentication prompt) can be distinguished from
     * unrecoverable ones. Migrate to [signV2] when convenient.
     *
     * This method is transitional and will be removed in a future release.
     */
    @Deprecated(
        message = "Superseded by signV2, which returns a SignResult.",
        replaceWith = ReplaceWith("signV2(payload, documentId)")
    )
    @Suppress("DEPRECATION")
    suspend fun sign(payload: ByteArray, documentId: String): ByteArray =
        when (val result = signV2(payload, documentId)) {
            is SignResult.Success -> result.signature
            is SignResult.Failure -> throw result.exception
        }

    /**
     * Signs the COSE `Sig_structure` [payload] with the private key bound to [documentId].
     *
     * Returns a [SignResult] describing the outcome of the operation.
     *
     * - [SignResult.Success] — signing succeeded; carries the DER-encoded ECDSA (ES256 / P-256)
     *   signature bytes.
     *
     * - [SignResult.Failure] with [CredentialSigningException.Recoverable] - signing did not
     *   complete but the session can continue.
     *   For example the user cancelling the local-authentication prompt.
     *   The SDK keeps the session active and ready for a retry.
     *
     * - [SignResult.Failure] with [CredentialSigningException.Unrecoverable] - signing failed for a
     *   reason the session cannot recover from. The SDK terminates the exchange and the journey
     *   ends in a failed state.
     */
    @Suppress("DEPRECATION")
    suspend fun signV2(payload: ByteArray, documentId: String): SignResult =
        SignResult.Success(sign(payload, documentId))
}

/**
 * Outcome of [CredentialProvider.signV2].
 */
sealed class SignResult {
    /** Signing succeeded; [signature] is the DER-encoded ECDSA (ES256 / P-256) signature. */
    class Success(val signature: ByteArray) : SignResult()

    /** Signing did not complete; [exception] categorises the failure. */
    data class Failure(val exception: CredentialSigningException) : SignResult()
}

data class CredentialRequest(val documentTypes: List<String>)

data class Credential(val id: String, val rawCredential: ByteArray)
