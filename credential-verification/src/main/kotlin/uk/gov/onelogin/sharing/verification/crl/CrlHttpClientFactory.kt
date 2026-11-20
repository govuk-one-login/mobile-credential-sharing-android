package uk.gov.onelogin.sharing.verification.crl

import android.content.Context
import java.io.File
import uk.gov.android.network.client.KtorHttpClient
import uk.gov.android.network.client.v3.RawCacheConfig
import uk.gov.android.network.client.v3.RawHttpClient
import uk.gov.android.network.useragent.UserAgent
import uk.gov.android.network.useragent.UserAgentGenerator

/**
 * Creates and caches one [RawHttpClient] per cache directory for the life of the process.
 *
 * Isolating [KtorHttpClient] construction here keeps concrete network types out of [CrlProvider]
 * (DIP) and guarantees a single shared cache per directory (SRP).
 */
internal object CrlHttpClientFactory {
    private const val CACHE_DIRECTORY = "crl-cache"
    private val clients = HashMap<String, RawHttpClient>()

    @Synchronized
    fun forContext(context: Context): RawHttpClient {
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