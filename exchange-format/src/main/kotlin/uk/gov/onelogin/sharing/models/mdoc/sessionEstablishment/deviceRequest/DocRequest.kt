package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

/**
 * Represents a request for a single document within a [DeviceRequest].
 * [DocRequest] allows the reader to specify exactly which namespaces and data elements it
 * wants to retrieve.
 *
 * @property itemsRequest An [ItemsRequest] object containing the document type,
 * the requested namespaces, and the specific data elements (attributes) being sought.
 */
data class DocRequest(val itemsRequest: ItemsRequest, val readerAuth: ByteArray? = null) {
    fun toDto(): DocRequestDto = DocRequestDto(
        itemsRequest = itemsRequest.toDto(),
        readerAuth = readerAuth
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocRequest

        if (itemsRequest != other.itemsRequest) return false
        if (readerAuth != null) {
            if (other.readerAuth == null) return false
            if (!readerAuth.contentEquals(other.readerAuth)) return false
        } else if (other.readerAuth != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = itemsRequest.hashCode()
        result = 31 * result + (readerAuth?.contentHashCode() ?: 0)
        return result
    }
}
