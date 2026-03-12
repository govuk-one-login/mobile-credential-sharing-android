package uk.gov.onelogin.sharing.ui.impl.di

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import kotlin.reflect.KClass
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.core.VerifierUiScope

/**
 * A concrete implementation of [MetroViewModelFactory] for the UI module.
 *
 * This factory is responsible for creating [ViewModel] instances by leveraging Metro's dependency
 * injection. it provides mapping for standard ViewModels and assisted-factory ViewModels.
 *
 */
@ContributesBinding(
    HolderUiScope::class,
    binding = binding<MetroViewModelFactory>()
)
@ContributesBinding(
    VerifierUiScope::class,
    binding = binding<MetroViewModelFactory>()
)
class UiMetroViewModelFactory(
    override val viewModelProviders: Map<KClass<out ViewModel>, Provider<ViewModel>>,
    override val assistedFactoryProviders: Map<
        KClass<out ViewModel>,
        Provider<ViewModelAssistedFactory>
        >,
    override val manualAssistedFactoryProviders:
    Map<KClass<out ManualViewModelAssistedFactory>, Provider<ManualViewModelAssistedFactory>>
) : MetroViewModelFactory()
