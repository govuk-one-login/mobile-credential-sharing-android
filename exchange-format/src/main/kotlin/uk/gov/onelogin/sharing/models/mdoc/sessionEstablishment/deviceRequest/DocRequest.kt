package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

data class DocRequest(
    val itemsRequest: ItemsRequest,
    val readerAuth: ByteArray? = null,
    val itemsRequestBytes: ByteArray? = null,
    val rawReaderAuth: ByteArray? = readerAuth
) {
    fun toDto(): DocRequestDto = DocRequestDto(
        itemsRequest = itemsRequest.toDto(),
        itemsRequestBytes = itemsRequestBytes,
        readerAuth = readerAuth ?: rawReaderAuth
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocRequest

        if (itemsRequest != other.itemsRequest) return false
        if (!itemsRequestBytes.contentEquals(other.itemsRequestBytes)) return false
        val thisAuth = readerAuth ?: rawReaderAuth
        val otherAuth = other.readerAuth ?: other.rawReaderAuth
        if (thisAuth != null) {
            if (otherAuth == null) return false
            if (!thisAuth.contentEquals(otherAuth)) return false
        } else if (otherAuth != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = itemsRequest.hashCode()
        result = 31 * result + (itemsRequestBytes?.contentHashCode() ?: 0)
        val auth = readerAuth ?: rawReaderAuth
        result = 31 * result + (auth?.contentHashCode() ?: 0)
        return result
    }
}
