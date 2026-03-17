## Sample Implementations: Consumer responsibilities

#### Holder role: Secure vault

This implementation demonstrates the boundary between the SDK and the consumer for the Holder role. The consumer acts as a secure vault: retrieving raw credentials and proxying signing requests to the Android Keystore. The SDK handles transport, Concise Binary Object Representation (CBOR) encoding, and consent UI.

```kotlin
import com.credentialsharing.sdk.*
import java.security.KeyStore
import java.security.Signature

class SecureVaultCredentialProvider : CredentialProvider {
    
    private val secureStorage = MySecureStorage() 
    
    /// 1. Provide Raw Credentials
    override suspend fun getCredentials(
        request: CredentialRequest
    ): List<Credential> {
        val credentials = mutableListOf<Credential>()
        
        for (docType in request.documentTypes) {
            val storedDoc = secureStorage.fetchDocument(docType)
            if (storedDoc != null) {
                credentials.add(
                    Credential(
                        id = storedDoc.id,
                        rawCredential = storedDoc.decryptedCborData
                    )
                )
            }
        }
        
        return credentials
    }
    
    /// 2. Sign Device Response
    override suspend fun sign(payload: ByteArray, documentId: String): ByteArray {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val privateKey = keyStore.getKey(documentId, null) as java.security.PrivateKey
        
        val signature = Signature.getInstance("SHA256withECDSA").apply {
            initSign(privateKey)
            update(payload)
        }
        return signature.sign()
    }
}
```

#### Verifier role: trust anchor & consumption

This implementation demonstrates how the consumer acts as a relying party. It provides trusted root certificate authorities (CA) to the SDK, defines the required data, and processes the decrypted, verified response, while the SDK handles the engagement and transport lifecycle.

```kotlin
import com.credentialsharing.sdk.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AgeVerificationActivity : AppCompatActivity() {

    // 1. Initialise Verifier with Trusted Root Certificates
    private val verifier: CredentialVerifier by lazy {
        val govRootCA = loadGovernmentRootCertificate()
        CredentialVerifier(trustedCertificates = listOf(govRootCA))
    }

    fun startAgeVerification() {
        lifecycleScope.launch {
            // 2. Define the Request
            val request = CredentialRequest(
                documentType = "org.iso.18013.5.1.mDL",
                requestedElements = listOf("age_over_18")
            )

            try {
                // 3. Start Verification Lifecycle
                // The SDK takes over, shows the camera, connects via BLE, and cryptographically validates the MSO.
                val verifiedData = verifier.requestDocument(
                    request = request, 
                    activity = this@AgeVerificationActivity
                )

                // 4. Process Verified Data
                val isOver18 = verifiedData.getValue("age_over_18") as? Boolean ?: false
                if (isOver18) {
                    println("Success: Customer is over 18.")
                } else {
                    println("Failure: Customer is under 18.")
                }
            } catch (e: Exception) {
                println("Verification interrupted or invalid: ${e.localizedMessage}")
            }
        }
    }

    private fun loadGovernmentRootCertificate(): java.security.cert.Certificate {
        // Load the public trusted root CA from app assets/raw
        TODO("Unimplemented")
    }
}
```