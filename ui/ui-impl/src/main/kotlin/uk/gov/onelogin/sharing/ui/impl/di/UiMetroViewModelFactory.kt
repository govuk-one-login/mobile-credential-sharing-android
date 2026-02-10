package uk.gov.onelogin.sharing.ui.impl.di

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import kotlin.reflect.KClass

/**
 * A concrete implementation of [MetroViewModelFactory] for the UI module.
 *
 * This factory is responsible for creating [ViewModel] instances by leveraging Metro's dependency
 * injection. it provides mapping for standard ViewModels and assisted-factory ViewModels.
 *
 */
@ContributesBinding(
    ViewModelScope::class,
    binding = binding<MetroViewModelFactory>()
)
@SingleIn(ViewModelScope::class)
class UiMetroViewModelFactory(
    override val viewModelProviders: Map<KClass<out ViewModel>, Provider<ViewModel>>,
    override val assistedFactoryProviders: Map<
        KClass<out ViewModel>,
        Provider<ViewModelAssistedFactory>
        >,
    override val manualAssistedFactoryProviders:
    Map<KClass<out ManualViewModelAssistedFactory>, Provider<ManualViewModelAssistedFactory>>
) : MetroViewModelFactory()
