package uk.gov.onelogin.sharing.verification.crl

import uk.gov.android.network.client.v3.RawHttpClient
import uk.gov.onelogin.sharing.verification.crl.CrlLookup.Available

/**
 * Inspects whether a fetched CRL was retained in the persistent cache and falls within its
 * signed validity window (`thisUpdate <= now < nextUpdate`).
 */
internal fun interface CrlCacheInspector {
    suspend fun assess(url: String, lookup: CrlLookup): CrlAssessment
}

/** How one CRL fares against the cache and the current clock. */
internal enum class CrlAssessment {
    /** Cached, readable and `thisUpdate <= now < nextUpdate`. */
    USABLE,

    /** The fetch failed, so nothing was cached. */
    UNAVAILABLE,

    /** The fetch succeeded but the response was not retained by the cache (e.g. `no-store`). */
    NOT_RETAINED,

    /** Cached but the bytes are not a valid CRL or have no `nextUpdate`. */
    UNREADABLE,

    /** Cached and readable, but `thisUpdate > now` (future-dated). */
    NOT_YET_VALID,

    /**
     * Cached and readable, but `now >= nextUpdate`. Triggers one forced server check
     * in [CrlProvider] before the URL is marked not ready.
     */
    EXPIRED
}

/**
 * Judges the entry stored in the cache rather than transient response bytes, ensuring
 * `no-store` responses cannot satisfy offline readiness.
 */
internal class DefaultCrlCacheInspector(
    private val client: RawHttpClient,
    private val decoder: CrlValidityDecoder,
    private val timeProvider: TimeProvider
) : CrlCacheInspector {

    override suspend fun assess(url: String, lookup: CrlLookup): CrlAssessment {
        if (lookup !is Available) return CrlAssessment.UNAVAILABLE
        val stored = client.cached(url) ?: return CrlAssessment.NOT_RETAINED
        val validity = decoder.readValidity(stored.body) ?: return CrlAssessment.UNREADABLE
        val now = timeProvider.currentTimeMillis()
        return when {
            now < validity.thisUpdate -> CrlAssessment.NOT_YET_VALID
            now >= validity.nextUpdate -> CrlAssessment.EXPIRED
            else -> CrlAssessment.USABLE
        }
    }
}