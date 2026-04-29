package uk.gov.onelogin.sharing.orchestration.holder.credential

/**
 * Holds the parsed and validated credential data after fetching from the Host App
 * and confirming the MSO docType matches the DeviceRequest.
 *
 * @param credentialId The unique identifier of the credential from the Host App.
 * @param nameSpaces The raw CBOR-encoded nameSpaces bytes from the credential.
 * @param issuerAuth The raw CBOR-encoded issuerAuth bytes (COSE_Sign1) from the credential.
 */
data class ValidatedCredential(
    val credentialId: String,
    val nameSpaces: ByteArray,
    val issuerAuth: ByteArray
)
