package uk.gov.onelogin.sharing.testapp.credential

import uk.gov.onelogin.sharing.orchestration.Credential
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.CredentialRequest
import uk.gov.onelogin.sharing.orchestration.CredentialSigningException
import uk.gov.onelogin.sharing.orchestration.SignResult

/**
 * Test App [CredentialProvider] whose [sign] operation always fails.
 *
 * [getCredentials] returns the normal Jane Doe credential, but every call to [sign] returns a
 * [SignResult.Failure] with [CredentialSigningException.Unrecoverable]. Used by the
 * "Jane Doe (signing failure)" option to reproduce a fatal signing failure.
 */
class FailingSignCredentialProvider(private val activeCredential: MockCredential) :
    CredentialProvider {

    override suspend fun getCredentials(request: CredentialRequest): List<Credential> = listOf(
        Credential(
            id = activeCredential.id,
            rawCredential = activeCredential.rawCredential
        )
    )

    override suspend fun sign(payload: ByteArray, documentId: String): SignResult =
        SignResult.Failure(
            CredentialSigningException.Unrecoverable(MockSignException.SignError())
        )
}
