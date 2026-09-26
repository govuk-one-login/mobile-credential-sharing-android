package uk.gov.onelogin.sharing.cryptoService.verifier

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

/**
 * Production implementation of [DocRequestBuilder] supporting single-candidate
 * and multi-candidate [DocRequest] payloads.
 */
@Inject
@ContributesBinding(AppScope::class)
class DocRequestBuilderImpl : DocRequestBuilder {

    override fun buildDocRequests(
        itemsRequest: ItemsRequest,
        itemsRequestBytes: ByteArray?,
        readerAuth: ByteArray?,
        buildItemsRequestBytes: (ItemsRequest) -> ByteArray
    ): List<DocRequest> = when (itemsRequest.docType) {
        TYPE_MULTI_UNSUPPORTED_AND_VALID -> listOf(
            createEvrcCandidate(readerAuth, buildItemsRequestBytes),
            createValidMdlCandidate(itemsRequest, readerAuth, itemsRequestBytes)
        )

        else -> listOf(
            DocRequest(
                itemsRequest = itemsRequest,
                readerAuth = readerAuth,
                itemsRequestBytes = itemsRequestBytes
            )
        )
    }

    private fun createEvrcCandidate(
        readerAuth: ByteArray?,
        buildItemsRequestBytes: (ItemsRequest) -> ByteArray
    ): DocRequest {
        val req = ItemsRequest(
            docType = DOC_TYPE_EVRC,
            nameSpaces = mapOf(NAMESPACE_ISO to mapOf(ATTR_VEHICLE_CATEGORY to false))
        )
        return DocRequest(
            itemsRequest = req,
            readerAuth = readerAuth,
            itemsRequestBytes = buildItemsRequestBytes(req)
        )
    }

    private fun createValidMdlCandidate(
        itemsRequest: ItemsRequest,
        readerAuth: ByteArray?,
        itemsRequestBytes: ByteArray?
    ): DocRequest {
        val mdlNameSpaces = if (itemsRequest.nameSpaces.containsKey(NAMESPACE_ISO)) {
            itemsRequest.nameSpaces
        } else {
            mapOf(NAMESPACE_ISO to (itemsRequest.nameSpaces.values.firstOrNull() ?: emptyMap()))
        }
        val req = ItemsRequest(docType = DOC_TYPE_MDL, nameSpaces = mdlNameSpaces)
        return DocRequest(
            itemsRequest = req,
            readerAuth = readerAuth,
            itemsRequestBytes = itemsRequestBytes
        )
    }

    companion object {
        const val TYPE_MULTI_UNSUPPORTED_AND_VALID = "MULTI_UNSUPPORTED_AND_VALID"

        const val DOC_TYPE_MDL = "org.iso.18013.5.1.mDL"
        const val DOC_TYPE_EVRC = "org.iso.18013.5.1.eVRC"
        const val NAMESPACE_ISO = "org.iso.18013.5.1"

        const val ATTR_VEHICLE_CATEGORY = "vehicle_category"
    }
}
