package uk.gov.onelogin.sharing.sdk

import uk.gov.onelogin.sharing.di.api.presenter.Credential
import uk.gov.onelogin.sharing.di.api.presenter.CredentialProvider
import uk.gov.onelogin.sharing.di.api.presenter.CredentialRequest

class FakeCredentialProvider : CredentialProvider {
    override suspend fun getCredentials(request: CredentialRequest): List<Credential> = emptyList()

    override suspend fun sign(payload: ByteArray, documentId: String): ByteArray = ByteArray(0)
}
