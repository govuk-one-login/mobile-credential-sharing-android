package uk.gov.onelogin.sharing.verification

interface VerifiableDocument {
    /**
     * The document type identifier.
     */
    val docType: String

    /**
     * The issuer-signed portion of the document.
     */
    val issuerSigned: IssuerSigned

    interface WithPresentation : VerifiableDocument {
        /**
         * The device-signed portion of the document; only present when the document was received
         * via proximity presentation.
         */
        val deviceSigned: DeviceSigned
    }
}
