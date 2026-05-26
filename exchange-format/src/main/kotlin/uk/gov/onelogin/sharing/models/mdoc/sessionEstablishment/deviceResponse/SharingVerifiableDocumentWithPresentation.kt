package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceSigned

data class SharingVerifiableDocumentWithPresentation(
    override val docType: String,
    override val issuerSigned: IssuerSigned,
    override val deviceSigned: DeviceSigned
) : SharingVerifiableDocument(docType, issuerSigned), VerifiableDocument.WithPresentation {

    constructor(
        document: VerifiableDocument,
        deviceSigned: DeviceSigned
    ) : this(
        docType = document.docType,
        issuerSigned = document.issuerSigned,
        deviceSigned = deviceSigned
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as SharingVerifiableDocumentWithPresentation

        if (docType != other.docType) return false
        if (issuerSigned != other.issuerSigned) return false
        if (deviceSigned != other.deviceSigned) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + docType.hashCode()
        result = 31 * result + issuerSigned.hashCode()
        result = 31 * result + deviceSigned.hashCode()
        return result
    }
}
