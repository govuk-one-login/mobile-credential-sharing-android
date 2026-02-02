package uk.gov.onelogin.sharing.di

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import uk.gov.logging.api.Logger

@DependencyGraph(AppScope::class)
interface CredentialSharingAppGraph {

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides applicationContext: Context,
            @Provides logger: Logger
        ): CredentialSharingAppGraph
    }

    fun applicationContext(): Context

    fun logger(): Logger
}
