package uk.gov.onelogin.sharing.verification.document

import uk.gov.onelogin.sharing.verification.document.models.DeviceSigned
import uk.gov.onelogin.sharing.verification.document.models.IssuerSigned

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