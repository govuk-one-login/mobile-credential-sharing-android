package uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential

/**
 * Result of parsing a raw credential's CBOR data.
 *
 * @param nameSpaces The raw CBOR-encoded nameSpaces bytes.
 * @param issuerAuth The raw CBOR-encoded issuerAuth bytes.
 * @param msoDocType The docType extracted from the MobileSecurityObject.
 */
data class ParsedRawCredential(
    val nameSpaces: ByteArray,
    val issuerAuth: ByteArray,
    val msoDocType: String
)
