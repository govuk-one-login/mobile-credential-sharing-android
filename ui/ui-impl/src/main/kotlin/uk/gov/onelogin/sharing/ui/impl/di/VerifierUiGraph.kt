package uk.gov.onelogin.sharing.ui.impl.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph

/**
 * Dependency graph for the UI implementation module.
 *
 * This graph is scoped to [VerifierUiScope] and extends [ViewModelGraph] to provide support
 * for Metro-powered ViewModels. It depends on the [CredentialSharingAppGraph] for
 * core application dependencies.
 */
@DependencyGraph(
    scope = VerifierUiScope::class
)
interface VerifierUiGraph : ViewModelGraph {

    /**
     * Factory for creating instances of [VerifierUiGraph].
     */
    @DependencyGraph.Factory
    fun interface Factory {
        /**
         * Creates a new [VerifierUiGraph] instance.
         *
         * @param appGraph The application-level dependency graph to include.
         * @return A configured [VerifierUiGraph] instance.
         */
        fun create(
            @Includes appGraph: CredentialSharingAppGraph,
            @Provides verifierOrchestrator: Orchestrator.Verifier
        ): VerifierUiGraph
    }

    fun appGraph(): CredentialSharingAppGraph

    fun verifierOrchestrator(): Orchestrator.Verifier
}
