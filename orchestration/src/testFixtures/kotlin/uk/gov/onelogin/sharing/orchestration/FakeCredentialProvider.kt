package uk.gov.onelogin.sharing.orchestration

import uk.gov.onelogin.Credential
import uk.gov.onelogin.CredentialProvider
import uk.gov.onelogin.CredentialRequest

class FakeCredentialProvider : CredentialProvider {
    override suspend fun getCredentials(request: CredentialRequest): List<Credential> = emptyList()

    override suspend fun sign(payload: ByteArray, documentId: String): ByteArray = ByteArray(0)
}
