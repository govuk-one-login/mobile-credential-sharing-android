package uk.gov.onelogin.sharing.testapp.credential

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
        val pemContent = activeCredential.privateKey.toString(Charsets.UTF_8)
            .lines()
            .filter { !it.startsWith("-----") && it.isNotBlank() }
            .joinToString("")
        val derBytes = java.util.Base64.getDecoder().decode(pemContent)

        val privateKey = KeyFactory.getInstance(ALGORITHM_EC)
            .generatePrivate(PKCS8EncodedKeySpec(derBytes))

        val derSignature = Signature.getInstance(SIGNING_ALGORITHM).run {
            initSign(privateKey)
            update(payload)
            sign()
        }

        return derToRawRS(derSignature)
    }

    /**
     * Converts a DER-encoded ECDSA signature to raw R||S format (64 bytes for P-256).
     *
     * DER structure: 0x30 <len> 0x02 <rLen> <r> 0x02 <sLen> <s>
     */
    private fun derToRawRS(der: ByteArray): ByteArray {
        var offset = 2
        val rLen = der[offset + 1].toInt() and 0xFF
        val rStart = offset + 2
        val r = der.copyOfRange(rStart, rStart + rLen)

        offset = rStart + rLen
        val sLen = der[offset + 1].toInt() and 0xFF
        val sStart = offset + 2
        val s = der.copyOfRange(sStart, sStart + sLen)

        val result = ByteArray(64)
        r.takeLast(32).toByteArray().copyInto(result, 32 - minOf(r.size, 32))
        s.takeLast(32).toByteArray().copyInto(result, 64 - minOf(s.size, 32))
        return result
    }
}
