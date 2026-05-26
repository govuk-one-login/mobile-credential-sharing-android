package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

/**
 * Represents a request for a digital credential as defined in ISO-18013-5.
 *
 * The device request is encoded in CBOR for transmission between holder and reader devices.
 *
 * @property version The version of the DeviceRequest structure.
 * @property docRequests A list of [DocRequest] objects, each specifying
 * a different document being requested and the specific data elements required.
 */
data class DeviceRequest(val version: String, val docRequests: List<DocRequest>)
