package uk.gov.onelogin.sharing.testapp

import android.content.Context
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import uk.gov.onelogin.sharing.orchestration.Credential
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.CredentialRequest

/**
 * Sample implementation of [CredentialProvider] for demonstration purposes.
 *
 * In a production app, this would retrieve actual credentials from secure storage
 * and use the Android Keystore for signing operations.
 */
class SampleCredentialProvider(context: Context) : CredentialProvider {

    private val activeCredential: MockCredential = MockCredentials.mockCredential(context)

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
        val pemText = String(activeCredential.privateKey)
        val derBytes = pemText
            .lines()
            .filter { !it.startsWith("-----") && it.isNotBlank() }
            .joinToString("")
            .let { Base64.getDecoder().decode(it) }

        val privateKey = KeyFactory.getInstance("EC")
            .generatePrivate(PKCS8EncodedKeySpec(derBytes))

        return Signature.getInstance("SHA256withECDSA").run {
            initSign(privateKey)
            update(payload)
            sign()
        }
    }
}
