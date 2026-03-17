package uk.gov.onelogin.sharing.orchestration

class FakeCredentialProvider : CredentialProvider {
    override suspend fun getCredentials(request: CredentialRequest): List<Credential> = emptyList()

    override suspend fun sign(payload: ByteArray, documentId: String): ByteArray = ByteArray(0)
}
