package uk.gov.onelogin.sharing.ui.impl.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

/**
 * Dependency graph for the UI implementation module.
 *
 * This graph is scoped to [ViewModelScope] and extends [ViewModelGraph] to provide support
 * for Metro-powered ViewModels. It depends on the [CredentialSharingAppGraph] for
 * core application dependencies.
 */
@DependencyGraph(
    scope = HolderUiScope::class
)
interface HolderCredentialSharingUiGraph : ViewModelGraph {

    /**
     * Factory for creating instances of [HolderCredentialSharingUiGraph].
     */
    @DependencyGraph.Factory
    fun interface Factory {
        /**
         * Creates a new [HolderCredentialSharingUiGraph] instance.
         *
         * @param appGraph The application-level dependency graph to include.
         * @return A configured [HolderCredentialSharingUiGraph] instance.
         */
        fun create(
            @Includes appGraph: CredentialSharingAppGraph,
            @Provides holderOrchestrator: Orchestrator.Holder
        ): HolderCredentialSharingUiGraph
    }

    fun appGraph(): CredentialSharingAppGraph

    fun holderOrchestrator(): Orchestrator.Holder
}
