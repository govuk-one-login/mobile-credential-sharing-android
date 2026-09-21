package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

data class DocRequest(
    val itemsRequest: ItemsRequest,
    val readerAuth: ByteArray? = null,
    val itemsRequestBytes: ByteArray? = null
) {
    fun toDto(): DocRequestDto = DocRequestDto(
        itemsRequest = itemsRequest.toDto(),
        itemsRequestBytes = itemsRequestBytes,
        readerAuth = readerAuth
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocRequest

        if (itemsRequest != other.itemsRequest) return false
        if (!itemsRequestBytes.contentEquals(other.itemsRequestBytes)) return false
        if (readerAuth != null) {
            if (other.readerAuth == null) return false
            if (!readerAuth.contentEquals(other.readerAuth)) return false
        } else if (other.readerAuth != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = itemsRequest.hashCode()
        result = 31 * result + (itemsRequestBytes?.contentHashCode() ?: 0)
        result = 31 * result + (readerAuth?.contentHashCode() ?: 0)
        return result
    }
}
