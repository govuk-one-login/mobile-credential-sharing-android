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

    override suspend fun signV2(payload: ByteArray, documentId: String): SignResult =
        SignResult.Success(ByteArray(0))
}
