package uk.gov.onelogin.sharing.verification.format.document

/**
 * @see uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSigned
 */
interface IssuerSigned {
    /**
     * The raw binary encoding of the IssuerAuth COSE_Sign1 structure.
     */
    val issuerAuth: ByteArray

    /**
     * A map from namespace string to an ordered list of raw Tag-24-encoded IssuerSignedItemBytes.
     */
    val nameSpaces: Map<String, List<ByteArray>>?
}
