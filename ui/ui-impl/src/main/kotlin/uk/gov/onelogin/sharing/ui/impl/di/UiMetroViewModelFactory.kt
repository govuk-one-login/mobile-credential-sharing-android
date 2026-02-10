package uk.gov.onelogin.sharing.ui.impl.di

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import kotlin.reflect.KClass

/**
 * A concrete implementation of [MetroViewModelFactory] for the UI module.
 *
 * This factory is responsible for creating [ViewModel] instances by leveraging Metro's dependency
 * injection. it provides mapping for standard ViewModels and assisted-factory ViewModels.
 *
 * @property viewModelProviders A map of [ViewModel] classes to their corresponding [Provider]s.
 * @property assistedFactoryProviders A map of [ViewModel] classes to their corresponding
 * [ViewModelAssistedFactory] providers.
 */
@ContributesBinding(
    AppScope::class,
    binding = binding<MetroViewModelFactory>()
)
@SingleIn(AppScope::class)
class UiMetroViewModelFactory(
    override val viewModelProviders: Map<KClass<out ViewModel>, Provider<ViewModel>>,
    override val assistedFactoryProviders: Map<
        KClass<out ViewModel>,
        Provider<ViewModelAssistedFactory>
        >
) : MetroViewModelFactory()
