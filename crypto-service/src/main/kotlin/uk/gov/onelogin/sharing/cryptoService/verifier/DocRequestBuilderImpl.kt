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

        TYPE_MULTI_CUSTOM_UK_AND_VALID -> listOf(
            createCustomUkPidCandidate(readerAuth, buildItemsRequestBytes),
            createValidMdlCandidate(itemsRequest, readerAuth, itemsRequestBytes)
        )

        TYPE_MULTI_UNTRUSTED_MISSING_PORTRAIT_VALID -> listOf(
            createUntrustedMdlCandidate(buildItemsRequestBytes),
            createMissingPortraitMdlCandidate(readerAuth, buildItemsRequestBytes),
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

    private fun createCustomUkPidCandidate(
        readerAuth: ByteArray?,
        buildItemsRequestBytes: (ItemsRequest) -> ByteArray
    ): DocRequest {
        val req = ItemsRequest(
            docType = DOC_TYPE_UK_PID,
            nameSpaces = mapOf(NAMESPACE_UK_PID to mapOf(ATTR_UK_NATIONAL_ID to false))
        )
        return DocRequest(
            itemsRequest = req,
            readerAuth = readerAuth,
            itemsRequestBytes = buildItemsRequestBytes(req)
        )
    }

    private fun createUntrustedMdlCandidate(
        buildItemsRequestBytes: (ItemsRequest) -> ByteArray
    ): DocRequest {
        val req = ItemsRequest(
            docType = DOC_TYPE_MDL,
            nameSpaces = mapOf(
                NAMESPACE_ISO to mapOf(
                    ATTR_PORTRAIT to false,
                    ATTR_AGE_OVER_21 to false
                )
            )
        )
        return DocRequest(
            itemsRequest = req,
            readerAuth = byteArrayOf(0x00, 0x01, 0x02),
            itemsRequestBytes = buildItemsRequestBytes(req)
        )
    }

    private fun createMissingPortraitMdlCandidate(
        readerAuth: ByteArray?,
        buildItemsRequestBytes: (ItemsRequest) -> ByteArray
    ): DocRequest {
        val req = ItemsRequest(
            docType = DOC_TYPE_MDL,
            nameSpaces = mapOf(
                NAMESPACE_ISO to mapOf(
                    ATTR_AGE_OVER_21 to false
                )
            )
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
        val mdlNameSpaces = mapOf(
            NAMESPACE_ISO to (itemsRequest.nameSpaces.values.firstOrNull() ?: emptyMap())
        )
        val req = ItemsRequest(docType = DOC_TYPE_MDL, nameSpaces = mdlNameSpaces)
        return DocRequest(
            itemsRequest = req,
            readerAuth = readerAuth,
            itemsRequestBytes = itemsRequestBytes
        )
    }

    companion object {
        const val TYPE_MULTI_UNSUPPORTED_AND_VALID = "MULTI_UNSUPPORTED_AND_VALID"
        const val TYPE_MULTI_CUSTOM_UK_AND_VALID = "MULTI_CUSTOM_UK_AND_VALID"
        const val TYPE_MULTI_UNTRUSTED_MISSING_PORTRAIT_VALID =
            "MULTI_UNTRUSTED_MISSING_PORTRAIT_VALID"

        const val DOC_TYPE_MDL = "org.iso.18013.5.1.mDL"
        const val DOC_TYPE_EVRC = "org.iso.18013.5.1.eVRC"
        const val DOC_TYPE_UK_PID = "org.uk.1800.5.0.pid"

        const val NAMESPACE_ISO = "org.iso.18013.5.1"
        const val NAMESPACE_UK_PID = "org.uk.1800.5.0"

        const val ATTR_PORTRAIT = "portrait"
        const val ATTR_VEHICLE_CATEGORY = "vehicle_category"
        const val ATTR_UK_NATIONAL_ID = "uk_national_id"
        const val ATTR_AGE_OVER_21 = "age_over_21"
    }
}
