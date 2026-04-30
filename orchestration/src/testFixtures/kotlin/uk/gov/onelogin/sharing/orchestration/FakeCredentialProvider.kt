package uk.gov.onelogin.sharing.orchestration

class FakeCredentialProvider : CredentialProvider {
    var credentialsToReturn: List<Credential> = emptyList()
    var getCredentialsException: Exception? = null
    var lastRequest: CredentialRequest? = null

    override suspend fun getCredentials(request: CredentialRequest): List<Credential> {
        lastRequest = request
        getCredentialsException?.let { throw it }
        return credentialsToReturn
    }

    override suspend fun sign(payload: ByteArray, documentId: String): ByteArray = ByteArray(0)
}
