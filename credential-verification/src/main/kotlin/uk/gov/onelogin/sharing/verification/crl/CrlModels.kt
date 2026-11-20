package uk.gov.onelogin.sharing.verification.crl

/** Outcome of [CrlProvider.refresh]. */
enum class CrlRefreshResult {
    /** Every configured CRL is cached, readable and within its signed dates. */
    READY,

    /** At least one configured CRL is missing, unreadable or outside its signed dates. */
    UPDATE_REQUIRED
}

/** Result of retrieving the bytes for one CRL URL. The bytes are not validated. */
internal sealed interface CrlLookup {
    data class Available(val bytes: ByteArray) : CrlLookup {
        override fun equals(other: Any?) = other is Available && bytes.contentEquals(other.bytes)

        override fun hashCode() = bytes.contentHashCode()
    }

    data class Unavailable(val reason: CrlLookupFailure) : CrlLookup
}

internal enum class CrlLookupFailure {
    /** Connection failure, DNS failure or timeout. */
    NETWORK,

    /**
     * Cache read or write failure. Not produced yet: the network library does not surface
     * storage errors (Ktor's FileStorage swallows them).
     */
    STORAGE,

    /** The server answered with something other than HTTP 200. */
    RESPONSE
}

/**
 * Retrieval seam between [CrlProvider] / `CoseVerification` and the underlying HTTP transport.
 * Implementations must not validate CRL contents.
 */
internal interface CrlFetcher {
    /**
     * @param url Absolute http(s) URL of a CRL distribution point
     * @param forceRevalidation When true, bypasses the HTTP freshness window and checks with the
     *   server even if the cached copy is still HTTP-fresh
     */
    suspend fun getCrl(url: String, forceRevalidation: Boolean = false): CrlLookup
}

/** Clock seam to support deterministic tests and future trusted-time injection (`WLT-SecReq-010`). */
fun interface TimeProvider {
    fun currentTimeMillis(): Long
}