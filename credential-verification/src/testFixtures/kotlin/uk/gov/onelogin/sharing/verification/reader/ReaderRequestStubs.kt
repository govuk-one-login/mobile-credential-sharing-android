package uk.gov.onelogin.sharing.verification.reader

import android.net.Uri
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

object ReaderRequestStubs {

    const val DEFAULT_DOC_TYPE = "org.iso.18013.5.1.mDL"
    const val DEFAULT_ORGANIZATION_NAME = "GOV.UK OneLogin"

    fun docRequest(
        docType: String = DEFAULT_DOC_TYPE,
        nameSpaces: Map<String, Map<String, Boolean>> = emptyMap(),
        readerAuth: ByteArray? = null
    ): DocRequest = DocRequest(
        itemsRequest = ItemsRequest(docType = docType, nameSpaces = nameSpaces),
        readerAuth = readerAuth
    )

    fun authenticatedReaderRequest(
        privacyPolicyUrl: Uri,
        docRequest: DocRequest = docRequest(),
        readerOrganizationName: String = DEFAULT_ORGANIZATION_NAME
    ): AuthenticatedReaderRequest = AuthenticatedReaderRequest(
        docRequest = docRequest,
        privacyPolicyUrl = privacyPolicyUrl,
        readerOrganizationName = readerOrganizationName
    )
}
