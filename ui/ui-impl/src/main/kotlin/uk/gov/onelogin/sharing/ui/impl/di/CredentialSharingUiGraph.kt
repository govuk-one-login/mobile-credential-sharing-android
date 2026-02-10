package uk.gov.onelogin.sharing.ui.impl.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

/**
 * Dependency graph for the UI implementation module.
 *
 * This graph is scoped to [AppScope] and extends [ViewModelGraph] to provide support
 * for Metro-powered ViewModels. It depends on the [CredentialSharingAppGraph] for
 * core application dependencies.
 */
@DependencyGraph(
    scope = AppScope::class
)
interface CredentialSharingUiGraph : ViewModelGraph {

    /**
     * Factory for creating instances of [CredentialSharingUiGraph].
     */
    @DependencyGraph.Factory
    fun interface Factory {
        /**
         * Creates a new [CredentialSharingUiGraph] instance.
         *
         * @param appGraph The application-level dependency graph to include.
         * @return A configured [CredentialSharingUiGraph] instance.
         */
        fun create(@Includes appGraph: CredentialSharingAppGraph): CredentialSharingUiGraph
    }

    /**
     * Provides the [MetroViewModelFactory] for creating ViewModels within this graph.
     */
    fun metroViewModelFactory(): MetroViewModelFactory
}
