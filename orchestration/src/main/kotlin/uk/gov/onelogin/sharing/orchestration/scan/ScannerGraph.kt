package uk.gov.onelogin.sharing.orchestration.scan

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import kotlin.reflect.KClass
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator

/**
 * Dependency graph for the scanner components.
 *
 * This graph is scoped to [VerifierUiScope] and extends [ViewModelGraph] to provide
 * [ScannerViewModel][uk.gov.onelogin.sharing.cameraService.scan.ScannerViewModel] resolution
 * without requiring the full UI layer.
 */
@DependencyGraph(scope = VerifierUiScope::class)
internal abstract class ScannerGraph : ViewModelGraph {

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides orchestrator: Orchestrator.Verifier): ScannerGraph
    }

    @Provides
    fun provideMetroViewModelFactory(
        viewModelProviders: Map<KClass<out ViewModel>, Provider<ViewModel>>,
        assistedFactoryProviders: Map<KClass<out ViewModel>, Provider<ViewModelAssistedFactory>>,
        manualAssistedFactoryProviders: Map<
            KClass<out ManualViewModelAssistedFactory>,
            Provider<ManualViewModelAssistedFactory>
            >
    ): MetroViewModelFactory = object : MetroViewModelFactory() {
        override val viewModelProviders = viewModelProviders
        override val assistedFactoryProviders = assistedFactoryProviders
        override val manualAssistedFactoryProviders = manualAssistedFactoryProviders
    }
}
