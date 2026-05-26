package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

/**
 * @param nameSpaces Map of namespace to list of original Tag 24 encoded IssuerSignedItemBytes.
 *                   Each ByteArray is the exact bytes from the credential to preserve MSO hash integrity.
 * @param issuerAuth The untouched issuerAuth bytes (COSE_Sign1) from the raw credential.
 */
data class IssuerSigned(val nameSpaces: Map<String, List<ByteArray>>?, val issuerAuth: ByteArray)
