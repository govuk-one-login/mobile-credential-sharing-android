package uk.gov.onelogin.sharing.testapp.credential

import uk.gov.onelogin.sharing.orchestration.Credential
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.CredentialRequest

/**
 * Test App [CredentialProvider] whose [sign] operation always fails.
 *
 * [getCredentials] returns the normal Jane Doe credential, but every call to [sign] throws
 * [MockSignException.SignError] and never returns a signature, regardless of the number of
 * attempts. Used by the "Jane Doe (signing failure)" option to reproduce a fatal signing failure
 * without a real local-authentication prompt.
 */
class FailingSignCredentialProvider(private val activeCredential: MockCredential) :
    CredentialProvider {

    override suspend fun getCredentials(request: CredentialRequest): List<Credential> = listOf(
        Credential(
            id = activeCredential.id,
            rawCredential = activeCredential.rawCredential
        )
    )

    override suspend fun sign(payload: ByteArray, documentId: String): ByteArray =
        throw MockSignException.SignError()
}
