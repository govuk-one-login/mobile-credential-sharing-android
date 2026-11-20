package uk.gov.onelogin.sharing.verification.crl

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The readiness check a journey runs before engagement. A seam so orchestrators can depend on
 * it without constructing a [CrlProvider], and tests can replace it.
 */
fun interface CrlReadinessCheck {
    /**
     * @see CrlProvider.refresh
     * @throws CrlConfigurationException if the host's CRL configuration is missing or unusable
     */
    suspend fun refresh(): CrlRefreshResult
}

/**
 * Creates the [CrlProvider] on the first [refresh] and keeps it, so a configuration error
 * reaches the caller when a journey starts, not when the graph is built. The provider is shared
 * across sessions; every refresh call re-evaluates the configured CRLs.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class LazyCrlReadinessCheck(
    private val appContext: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CrlReadinessCheck {
    private val provider by lazy { CrlProvider(appContext) }

    /** Reading the asset, building the client and the requests all run on [ioDispatcher]. */
    override suspend fun refresh(): CrlRefreshResult = withContext(ioDispatcher) {
        provider.refresh()
    }
}