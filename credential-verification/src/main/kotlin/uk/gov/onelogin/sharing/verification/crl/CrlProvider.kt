package uk.gov.onelogin.sharing.verification.crl

import android.content.Context
import java.io.File
import java.io.IOException
import uk.gov.android.network.client.KtorHttpClient
import uk.gov.android.network.client.v3.RawCacheConfig
import uk.gov.android.network.client.v3.RawHttpClient
import uk.gov.android.network.useragent.UserAgent
import uk.gov.android.network.useragent.UserAgentGenerator
import uk.gov.onelogin.sharing.verification.crl.CrlLookup.Available
import uk.gov.onelogin.sharing.verification.crl.CrlLookup.Unavailable

/**
 * Retrieves and readiness-checks CRLs.
 *
 * - [getCrl] is the retrieval layer. It calls the network library, which owns the HTTP cache
 *   (freshness, conditional requests, stale fallback). It returns bytes and validates nothing.
 * - [refresh] is the readiness check. It calls [getCrl] for every configured URL, then inspects
 *   the cache entry and the CRL's signed dates.
 *
 * All instances in a process share one client, and so one cache, per cache directory.
 * Construction reads the host's `credential-sharing-crl.json` and makes no network request.
 *
 * @throws CrlConfigurationException from the public constructor if the host resource is missing
 *   or unusable
 */
class CrlProvider internal constructor(
    private val prefetchUrls: List<String>,
    private val client: RawHttpClient,
    private val now: () -> Long = System::currentTimeMillis
) {
    constructor(applicationContext: Context) : this(
        prefetchUrls = CrlConfiguration.load(applicationContext).prefetchUrls,
        client = SharedCrlHttpClient.get(applicationContext)
    )

    /**
     * Checks that every configured URL has a usable CRL: cached, readable, and
     * `thisUpdate <= now < nextUpdate`. Does not check signatures or revocation.
     *
     * - Every URL is processed, so one failure does not stop the others being cached.
     * - A signed-expired CRL gets one forced server check before it counts as expired.
     * - An empty list returns [CrlRefreshResult.READY] without any request.
     * - Network, storage and response failures are reflected in the result, never thrown.
     *   Cancellation propagates.
     *
     * URLs discovered later through [getCrl] are not configured and do not affect the result.
     */
    suspend fun refresh(): CrlRefreshResult {
        var ready = true
        for (url in prefetchUrls) {
            if (!isUsable(url)) ready = false
        }
        return if (ready) CrlRefreshResult.READY else CrlRefreshResult.UPDATE_REQUIRED
    }

    /**
     * Returns the bytes for one CRL URL. Any URL is accepted, including ones not in the
     * configured list. They are cached by the shared client but never added to the list.
     *
     * @param forceRevalidation check with the server even if the cached copy is HTTP-fresh
     */
    internal suspend fun getCrl(url: String, forceRevalidation: Boolean = false): CrlLookup {
        val response = try {
            client.get(url = url, forceRevalidation = forceRevalidation)
        } catch (_: IOException) {
            return Unavailable(CrlLookupFailure.NETWORK)
        }
        return if (response.status == HTTP_OK) {
            Available(response.body)
        } else {
            Unavailable(CrlLookupFailure.RESPONSE)
        }
    }

    private suspend fun isUsable(url: String): Boolean {
        var assessment = assess(url, getCrl(url))
        if (assessment == Assessment.EXPIRED) {
            assessment = assess(url, getCrl(url, forceRevalidation = true))
        }
        return assessment == Assessment.USABLE
    }

    /**
     * Judges the entry that is in the cache, not the bytes just returned. The cache is what an
     * offline journey will use, and a response that was not retained (for example `no-store`)
     * cannot satisfy readiness.
     */
    private suspend fun assess(url: String, lookup: CrlLookup): Assessment {
        if (lookup !is Available) return Assessment.UNAVAILABLE
        val stored = client.cached(url) ?: return Assessment.NOT_RETAINED
        val validity = CrlDecoder.readValidity(stored.body) ?: return Assessment.UNREADABLE
        val time = now()
        return when {
            time < validity.thisUpdate -> Assessment.NOT_YET_VALID
            time >= validity.nextUpdate -> Assessment.EXPIRED
            else -> Assessment.USABLE
        }
    }

    private enum class Assessment { USABLE, UNAVAILABLE, NOT_RETAINED, UNREADABLE, NOT_YET_VALID, EXPIRED }

    private companion object {
        const val HTTP_OK = 200
    }
}

/** One client, and so one cache, per cache directory for the life of the process. */
internal object SharedCrlHttpClient {
    private const val CACHE_DIRECTORY = "crl-cache"
    private val clients = HashMap<String, RawHttpClient>()

    @Synchronized
    fun get(context: Context): RawHttpClient {
        val directory = File(context.applicationContext.noBackupFilesDir, CACHE_DIRECTORY)
        return clients.getOrPut(directory.absolutePath) {
            KtorHttpClient(
                userAgentGenerator = SdkUserAgent,
                rawCache = RawCacheConfig(directory)
            )
        }
    }

    private object SdkUserAgent : UserAgentGenerator {
        override fun setUserAgent(userAgent: UserAgent) = Unit

        override fun getUserAgent() = "GOV.UK-Wallet-Credential-Sharing-SDK"
    }
}
