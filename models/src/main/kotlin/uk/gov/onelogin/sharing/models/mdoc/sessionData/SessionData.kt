package uk.gov.onelogin.sharing.models.mdoc.sessionData

/**
 * Represents the ISO 18013-5 `SessionData` transport envelope.
 *
 * This is the top-level message structure used to communicate with the Verifier over the BLE
 * transport layer. It can carry an encrypted credential payload, a termination/error status code,
 * or both.
 *
 * ```
 * SessionData = {
 *     ? "data" : bstr,
 *     ? "status" : uint
 * }
 * ```
 *
 * @param data The encrypted ciphertext and authentication tag, or null if not present.
 * @param status The session data status code, or null if not present.
 */
data class SessionData(val data: ByteArray? = null, val status: SessionDataStatus? = null) {
    /**
     * @return `false` when [data] is null. Otherwise, `true`.
     */
    fun hasData(): Boolean = data != null

    /**
     * @return `true` when [status] equals null or [SessionDataStatus.OK]. Otherwise, `false`.
     */
    fun hasOkStatus(): Boolean = status?.let { it == SessionDataStatus.OK } ?: true

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SessionData

        if (!data.contentEquals(other.data)) return false
        if (status != other.status) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data?.contentHashCode() ?: 0
        result = 31 * result + (status?.hashCode() ?: 0)
        return result
    }
}
