package uk.gov.onelogin.sharing.testapp

import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import uk.gov.onelogin.sharing.orchestration.Credential
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.CredentialRequest

const val ALGORITHM_EC = "EC"
const val SIGNING_ALGORITHM = "SHA256withECDSA"

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
     * Instantiates the EC private key from the raw PKCS#8 bytes stored in the active
     * [MockCredential] and signs the [payload] using SHA256withECDSA. In a production app,
     * signing would be delegated to the Android Keystore so the private key never leaves
     * secure hardware.
     */
    override suspend fun sign(payload: ByteArray, documentId: String): ByteArray {
        val privateKey = KeyFactory.getInstance(ALGORITHM_EC)
            .generatePrivate(PKCS8EncodedKeySpec(activeCredential.privateKey))

        return Signature.getInstance(SIGNING_ALGORITHM).run {
            initSign(privateKey)
            update(payload)
            sign()
        }
    }
}
