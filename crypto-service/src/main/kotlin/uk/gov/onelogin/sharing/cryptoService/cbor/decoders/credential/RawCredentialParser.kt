package uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential

/**
 * Parses a raw credential's CBOR bytes, extracting the nameSpaces, issuerAuth,
 * and the docType from the MobileSecurityObject.
 */
fun interface RawCredentialParser {
    /**
     * @param rawCredential The CBOR-encoded credential bytes from the Host App.
     * @return [ParsedRawCredential] containing the extracted components.
     * @throws RawCredentialParsingException if the CBOR structure is invalid.
     */
    fun parse(rawCredential: ByteArray): ParsedRawCredential
}
