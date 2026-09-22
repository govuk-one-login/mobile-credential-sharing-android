package uk.gov.onelogin.sharing.testapp.credential

import uk.gov.onelogin.sharing.orchestration.Credential
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.CredentialRequest
import uk.gov.onelogin.sharing.orchestration.SignResult

/**
 * Sample implementation of [CredentialProvider] for demonstration purposes.
 *
 * In a production app, this would retrieve actual credentials from secure storage
 * and use the Android Keystore for signing operations.
 */
class SampleCredentialProvider(private val activeCredential: MockCredential) : CredentialProvider {

    override suspend fun getCredentials(request: CredentialRequest): List<Credential> = listOf(
        Credential(
            id = activeCredential.id,
            rawCredential = activeCredential.rawCredential
        )
    )

    /**
     * Mock signing implementation for use in the Test App only.
     *
     * Signs the [payload] using the EC private key stored in the active [MockCredential].
     */
    override suspend fun sign(payload: ByteArray, documentId: String): SignResult =
        SignResult.Success(signWithEcPrivateKey(payload, activeCredential.privateKey))
}
