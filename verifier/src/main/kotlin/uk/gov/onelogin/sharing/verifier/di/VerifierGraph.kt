package uk.gov.onelogin.sharing.verifier.di

import android.content.Context
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.logging.api.Logger
import uk.gov.logging.impl.AndroidLogger
import uk.gov.onelogin.sharing.core.logger.SystemCrashLogger

@DependencyGraph(ViewModelScope::class)
interface VerifierGraph : ViewModelGraph {
    @Provides
    fun provideLogger(): Logger = AndroidLogger(
        SystemCrashLogger()
    )

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides context: Context): VerifierGraph
    }
}
