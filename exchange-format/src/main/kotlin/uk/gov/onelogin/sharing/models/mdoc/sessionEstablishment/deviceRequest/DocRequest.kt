package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

/**
 * Represents a request for a single document within a [DeviceRequest].
 * [DocRequest] allows the reader to specify exactly which namespaces and data elements it
 * wants to retrieve.
 *
 * @property itemsRequest An [ItemsRequest] object containing the document type,
 * the requested namespaces, and the specific data elements (attributes) being sought.
 */
data class DocRequest(val itemsRequest: ItemsRequest)
