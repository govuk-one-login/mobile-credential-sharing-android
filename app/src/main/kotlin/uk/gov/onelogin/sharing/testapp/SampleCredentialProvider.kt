package uk.gov.onelogin.sharing.testapp

import uk.gov.onelogin.sharing.ui.api.Credential
import uk.gov.onelogin.sharing.ui.api.CredentialProvider
import uk.gov.onelogin.sharing.ui.api.CredentialRequest

/**
 * Sample implementation of [CredentialProvider] for demonstration purposes.
 *
 * In a production app, this would retrieve actual credentials from secure storage
 * and use the Android Keystore for signing operations.
 */
class SampleCredentialProvider : CredentialProvider {
    override suspend fun getCredentials(request: CredentialRequest): List<Credential> {
        // Sample implementation - returns mock credentials
        return request.documentTypes.map { docType ->
            Credential(
                id = "sample-$docType",
                rawCredential = ByteArray(0) // Placeholder
            )
        }
    }

    override suspend fun sign(payload: ByteArray, documentId: String): ByteArray {
        // Sample implementation - would use Android Keystore in production
        return ByteArray(PLACEHOLDER_SIGNATURE_SIZE) // Placeholder signature
    }

    private companion object {
        private const val PLACEHOLDER_SIGNATURE_SIZE = 64
    }
}
