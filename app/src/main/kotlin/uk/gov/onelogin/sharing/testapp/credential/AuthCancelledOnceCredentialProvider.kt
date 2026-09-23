package uk.gov.onelogin.sharing.testapp.credential

import java.util.concurrent.atomic.AtomicBoolean
import uk.gov.onelogin.sharing.orchestration.Credential
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.CredentialRequest
import uk.gov.onelogin.sharing.orchestration.CredentialSigningException

/**
 * Test App [CredentialProvider] whose first [sign] attempt reports a cancelled
 * local-authentication prompt, then signs normally on subsequent attempts.
 *
 * [getCredentials] returns the normal Jane Doe credential.
 * The first call to [sign] returns a [CredentialSigningException.Recoverable] and the
 * second and subsequent calls sign successfully using the Jane Doe private key.
 * Used by the "Jane Doe (authentication cancelled once)" option to reproduce a cancelled
 * authentication.
 */
class AuthCancelledOnceCredentialProvider(private val activeCredential: MockCredential) :
    CredentialProvider {

    private val hasCancelledOnce = AtomicBoolean(false)

    override suspend fun getCredentials(request: CredentialRequest): List<Credential> = listOf(
        Credential(
            id = activeCredential.id,
            rawCredential = activeCredential.rawCredential
        )
    )

    override suspend fun sign(payload: ByteArray, documentId: String): ByteArray {
        if (hasCancelledOnce.compareAndSet(false, true)) {
            throw CredentialSigningException.Recoverable(MockSignException.LocalAuthCancelled())
        }
        return signWithEcPrivateKey(
            payload = payload,
            privateKeyBytes = activeCredential.privateKey
        )
    }
}
