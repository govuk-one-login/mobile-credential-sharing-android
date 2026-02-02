package uk.gov.onelogin.sharing.verifier.di

import android.content.Context
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

@DependencyGraph(ViewModelScope::class)
interface VerifierGraph : ViewModelGraph {

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Includes appGraph: CredentialSharingAppGraph,
            @Provides context: Context
        ): VerifierGraph
    }
}
