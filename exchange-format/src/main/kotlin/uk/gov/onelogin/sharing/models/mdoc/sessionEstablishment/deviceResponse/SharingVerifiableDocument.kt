package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument

open class SharingVerifiableDocument(
    override val docType: String,
    override val issuerSigned: IssuerSigned
) : VerifiableDocument {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SharingVerifiableDocument

        if (docType != other.docType) return false
        if (issuerSigned != other.issuerSigned) return false

        return true
    }

    override fun hashCode(): Int {
        var result = docType.hashCode()
        result = 31 * result + issuerSigned.hashCode()
        return result
    }
}
