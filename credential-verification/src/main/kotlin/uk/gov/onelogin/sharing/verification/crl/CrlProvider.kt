package uk.gov.onelogin.sharing.verification.crl

import android.content.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import uk.gov.android.network.client.v3.RawHttpClient

/**
 * Retrieves and readiness-checks CRLs.
 *
 * - [getCrl] is the retrieval layer. It delegates HTTP caching (freshness, conditional requests,
 *   stale fallback) to the network library and returns unmodified bytes without validation.
 * - [refresh] is the readiness check. It calls [getCrl] for every configured URL, then inspects
 *   the cached entry and the CRL's signed dates.
 *
 * All instances in a process share one client, and so one cache, per cache directory.
 * Construction reads the host's `credential-sharing-crl.json` and makes no network request.
 *
 * @throws CrlConfigurationException from the public constructor if the host resource is missing
 *   or unusable
 */class CrlProvider internal constructor(
    private val prefetchUrls: List<String>,
    internal val fetcher: CrlFetcher,
    private val inspector: CrlCacheInspector
) : CrlReadinessCheck {

    constructor(applicationContext: Context) : this(
        configurationSource = AssetCrlConfigurationSource(applicationContext),
        client = CrlHttpClientFactory.forContext(applicationContext)
    )

    internal constructor(
        configurationSource: CrlConfigurationSource,
        client: RawHttpClient,
        decoder: CrlValidityDecoder = CrlDecoder,
        timeProvider: TimeProvider = TimeProvider { System.currentTimeMillis() }
    ) : this(
        prefetchUrls = configurationSource.load().prefetchUrls,
        fetcher = RawHttpCrlFetcher(client),
        inspector = DefaultCrlCacheInspector(client, decoder, timeProvider)
    )

    internal constructor(
        prefetchUrls: List<String>,
        client: RawHttpClient,
        decoder: CrlValidityDecoder = CrlDecoder,
        now: () -> Long = System::currentTimeMillis
    ) : this(
        prefetchUrls = prefetchUrls,
        fetcher = RawHttpCrlFetcher(client),
        inspector = DefaultCrlCacheInspector(client, decoder, TimeProvider { now() })
    )

    override suspend fun refresh(): CrlRefreshResult = coroutineScope {
        if (prefetchUrls.isEmpty()) return@coroutineScope CrlRefreshResult.READY

        val allUsable = prefetchUrls
            .map { url -> async { isUsable(url) } }
            .awaitAll()
            .all { it }

        if (allUsable) CrlRefreshResult.READY else CrlRefreshResult.UPDATE_REQUIRED
    }

    /**
     * Returns the bytes for one CRL URL. Any URL is accepted, including ones not in the
     * configured list. They are cached by the shared client but never added to the list.
     */
    internal suspend fun getCrl(url: String, forceRevalidation: Boolean = false): CrlLookup =
        fetcher.getCrl(url, forceRevalidation)

    private suspend fun isUsable(url: String): Boolean {
        var assessment = inspector.assess(url, getCrl(url))
        if (assessment == CrlAssessment.EXPIRED) {
            assessment = inspector.assess(url, getCrl(url, forceRevalidation = true))
        }
        return assessment == CrlAssessment.USABLE
    }
}