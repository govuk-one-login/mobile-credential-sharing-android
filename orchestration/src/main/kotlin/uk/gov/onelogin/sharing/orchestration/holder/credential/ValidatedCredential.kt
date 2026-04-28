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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ValidatedCredential
        return credentialId == other.credentialId &&
            nameSpaces.contentEquals(other.nameSpaces) &&
            issuerAuth.contentEquals(other.issuerAuth)
    }

    override fun hashCode(): Int {
        var result = credentialId.hashCode()
        result = 31 * result + nameSpaces.contentHashCode()
        result = 31 * result + issuerAuth.contentHashCode()
        return result
    }
}
