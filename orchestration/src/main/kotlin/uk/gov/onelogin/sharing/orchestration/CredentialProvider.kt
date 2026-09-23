package uk.gov.onelogin.sharing.orchestration

/**
 * Provider interface for Holder role.
 * Host app implements this to supply credentials and signatures.
 */
interface CredentialProvider {
    suspend fun getCredentials(request: CredentialRequest): List<Credential>

    /**
     * Signs the COSE `Sig_structure` [payload] with the private key bound to [documentId].
     *
     * On success, returns the DER-encoded ECDSA (ES256 / P-256) signature bytes. On failure,
     * throw a [CredentialSigningException] describing the outcome so the SDK can react correctly:
     *
     * - [CredentialSigningException.Recoverable] — signing did not complete but the session can
     *   continue. For example when the user cancels local-authentication prompt.
     *   Neutral: the SDK keeps the session active and does not progress or show an error
     * - [CredentialSigningException.Unrecoverable] — signing failed for a reason the session cannot
     *   recover from. Fatal: the SDK terminates the exchange and the journey ends in a failed state.
     */
    @Throws(
        CredentialSigningException.Recoverable::class,
        CredentialSigningException.Unrecoverable::class
    )
    suspend fun sign(payload: ByteArray, documentId: String): ByteArray
}

data class CredentialRequest(val documentTypes: List<String>)

data class Credential(val id: String, val rawCredential: ByteArray)
