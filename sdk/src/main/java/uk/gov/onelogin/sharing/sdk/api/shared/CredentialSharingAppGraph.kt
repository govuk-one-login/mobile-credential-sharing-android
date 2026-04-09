package uk.gov.onelogin.sharing.sdk.api.shared
import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.permission.PermissionCheckerV2

@DependencyGraph(AppScope::class)
interface CredentialSharingAppGraph {

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides applicationContext: Context,
            @Provides logger: Logger,
            @Provides permissionCheckerV2: PermissionCheckerV2
        ): CredentialSharingAppGraph
    }

    fun applicationContext(): Context

    fun logger(): Logger

    fun permissionChecker(): PermissionCheckerV2
}
