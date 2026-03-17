package uk.gov.onelogin.sharing.orchestration

/**
 * Provider interface for Holder role.
 * Host app implements this to supply credentials and signatures.
 */
interface CredentialProvider {
    suspend fun getCredentials(request: CredentialRequest): List<Credential>
    suspend fun sign(payload: ByteArray, documentId: String): ByteArray
}

data class CredentialRequest(val documentTypes: List<String>)

data class Credential(val id: String, val rawCredential: ByteArray)
