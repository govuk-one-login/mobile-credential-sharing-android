package uk.gov.onelogin.sharing.testapp

/**
 * Data model for a mock credential used in the Test App.
 *
 * @property id A unique string identifier.
 * @property displayName A string to identify the credential in the Test App UI.
 * @property rawCredential The full, decrypted raw CBOR data byte string for the mDL.
 * @property privateKey The raw bytes of the private key that corresponds to the public key
 * embedded in the mock credential's MSO.
 */
data class MockCredential(
    val id: String,
    val displayName: String,
    val rawCredential: ByteArray,
    val privateKey: ByteArray
)
