package uk.gov.onelogin.sharing.ui.impl.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph

/**
 * Dependency graph for the UI implementation module.
 *
 * This graph is scoped to [ViewModelScope] and extends [ViewModelGraph] to provide support
 * for Metro-powered ViewModels. It depends on the [CredentialSharingAppGraph] for
 * core application dependencies.
 */
@DependencyGraph(
    scope = VerifierUiScope::class
)
interface VerifierCredentialSharingUiGraph : ViewModelGraph {

    /**
     * Factory for creating instances of [VerifierCredentialSharingUiGraph].
     */
    @DependencyGraph.Factory
    fun interface Factory {
        /**
         * Creates a new [VerifierCredentialSharingUiGraph] instance.
         *
         * @param appGraph The application-level dependency graph to include.
         * @return A configured [VerifierCredentialSharingUiGraph] instance.
         */
        fun create(
            @Includes appGraph: CredentialSharingAppGraph,
            @Provides verifierOrchestrator: Orchestrator.Verifier
        ): VerifierCredentialSharingUiGraph
    }

    fun appGraph(): CredentialSharingAppGraph

    fun verifierOrchestrator(): Orchestrator.Verifier
}
