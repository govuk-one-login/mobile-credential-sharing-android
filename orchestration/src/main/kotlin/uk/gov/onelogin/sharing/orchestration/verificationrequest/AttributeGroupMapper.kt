package uk.gov.onelogin.sharing.orchestration.verificationrequest

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

fun AttributeGroup.toItemsRequest(documentType: DocumentType): ItemsRequest = ItemsRequest(
    docType = documentType.value,
    nameSpaces = mapOf(
        namespaceFor(documentType) to attributes.entries.associate { (attr, intentToRetain) ->
            attr.value to intentToRetain
        }
    )
)

fun AttributeGroup.toItemsRequest(documentType: String): ItemsRequest =
    toItemsRequest(resolveDocumentType(documentType))

internal fun resolveDocumentType(documentType: String): DocumentType = when (documentType) {
    DocumentType.Mdl.value -> DocumentType.Mdl
    else -> DocumentType.Custom(documentType)
}

private fun namespaceFor(documentType: DocumentType): String = when (documentType) {
    is DocumentType.Mdl -> DocumentType.Mdl.NAMESPACE
    is DocumentType.Custom -> documentType.value
}
