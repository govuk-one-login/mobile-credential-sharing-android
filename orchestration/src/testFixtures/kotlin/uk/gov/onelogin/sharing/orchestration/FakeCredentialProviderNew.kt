package uk.gov.onelogin.sharing.orchestration

import uk.gov.onelogin.orchestration.Credential
import uk.gov.onelogin.orchestration.CredentialProviderNew
import uk.gov.onelogin.orchestration.CredentialRequest

class FakeCredentialProviderNew : CredentialProviderNew {
    override suspend fun getCredentials(request: CredentialRequest): List<Credential> = emptyList()

    override suspend fun sign(payload: ByteArray, documentId: String): ByteArray = ByteArray(0)
}
