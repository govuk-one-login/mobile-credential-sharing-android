package uk.gov.onelogin.sharing.verification.crl

import android.content.Context
import java.io.File
import uk.gov.android.network.client.KtorHttpClient
import uk.gov.android.network.client.v3.RawCacheConfig
import uk.gov.android.network.client.v3.RawHttpClient
import uk.gov.android.network.useragent.UserAgent
import uk.gov.android.network.useragent.UserAgentGenerator

/**
 * Spike: downloads every URL in the host app's `credential-sharing-crl.json`.
 *
 * To be folded into `CrlProvider.refresh()`. The cache is owned by the shared client, so the
 * downloaded CRLs are reusable by later lookups, including from other instances of this class.
 *
 * Reading the configuration happens at construction and makes no network request.
 *
 * @throws CrlConfigurationException from the constructor if the host resource is missing or unusable
 */
class CrlPrefetchDownloader internal constructor(
    private val urls: List<String>,
    private val client: CrlEndpointClient
) {
    constructor(context: Context) : this(
        urls = CrlConfiguration.load(context.applicationContext).prefetchUrls,
        client = CrlEndpointClient(SharedCrlHttpClient.get(context.applicationContext))
    )

    /**
     * Downloads each configured URL once, in order. A failure for one URL is recorded and does
     * not stop the others. Cancellation propagates.
     */
    suspend fun downloadAll(): Map<String, CrlFetchResult> {
        val results = LinkedHashMap<String, CrlFetchResult>()
        for (url in urls) {
            results[url] = client.fetch(url)
        }
        return results
    }
}

/** One client, and so one cache, per cache directory for the life of the process. */
internal object SharedCrlHttpClient {
    private const val CACHE_DIRECTORY = "crl-cache"
    private val clients = HashMap<String, RawHttpClient>()

    @Synchronized
    fun get(context: Context): RawHttpClient {
        val directory = File(context.noBackupFilesDir, CACHE_DIRECTORY)
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
