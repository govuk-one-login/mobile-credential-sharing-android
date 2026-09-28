package uk.gov.onelogin.sharing.cryptoService.verifier

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

/**
 * Component interface responsible for building candidate [DocRequest] lists for [DeviceRequest].
 */
fun interface DocRequestBuilder {
    /**
     * Constructs candidate [DocRequest] instances based on [itemsRequest.docType].
     *
     * @param itemsRequest The primary requested [ItemsRequest].
     * @param itemsRequestBytes The encoded CBOR bytes for [itemsRequest].
     * @param readerAuth The COSE_Sign1 signature bytes.
     * @param buildItemsRequestBytes Function to construct CBOR Tag 24 bytes for custom candidate itemsRequests.
     * @return The list of [DocRequest] candidates for the [DeviceRequest].
     */
    fun buildDocRequests(
        itemsRequest: ItemsRequest,
        itemsRequestBytes: ByteArray?,
        readerAuth: ByteArray?,
        buildItemsRequestBytes: (ItemsRequest) -> ByteArray
    ): List<DocRequest>
}
