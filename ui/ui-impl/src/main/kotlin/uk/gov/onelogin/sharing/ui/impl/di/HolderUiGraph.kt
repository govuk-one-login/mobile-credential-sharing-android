package uk.gov.onelogin.sharing.ui.impl.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph

/**
 * Dependency graph for the UI implementation module.
 *
 * This graph is scoped to [HolderUiScope] and extends [ViewModelGraph] to provide support
 * for Metro-powered ViewModels. It depends on the [CredentialSharingAppGraph] for
 * core application dependencies.
 */
@DependencyGraph(
    scope = HolderUiScope::class
)
interface HolderUiGraph : ViewModelGraph {

    /**
     * Factory for creating instances of [HolderUiGraph].
     */
    @DependencyGraph.Factory
    fun interface Factory {
        /**
         * Creates a new [HolderUiGraph] instance.
         *
         * @param appGraph The application-level dependency graph to include.
         * @return A configured [HolderUiGraph] instance.
         */
        fun create(
            @Includes appGraph: CredentialSharingAppGraph,
            @Provides holderOrchestrator: Orchestrator.Holder
        ): HolderUiGraph
    }

    fun appGraph(): CredentialSharingAppGraph

    fun holderOrchestrator(): Orchestrator.Holder
}
