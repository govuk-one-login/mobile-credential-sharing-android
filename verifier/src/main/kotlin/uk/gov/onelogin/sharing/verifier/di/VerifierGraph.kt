package uk.gov.onelogin.sharing.verifier.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

@DependencyGraph(ViewModelScope::class)
interface VerifierGraph : ViewModelGraph {

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Includes appGraph: CredentialSharingAppGraph): VerifierGraph
    }
}
