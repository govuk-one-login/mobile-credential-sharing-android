package uk.gov.onelogin.sharing.holder.prerequisites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class HolderPrerequisitesViewModel(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    orchestrator: Orchestrator.Holder
) : ViewModel() {

    val holderSessionState: StateFlow<HolderSessionState> = orchestrator.holderSessionState

    val events: SharedFlow<NavigationEvent> = holderSessionState
        .mapNotNull { state ->
            when (state) {
                is HolderSessionState.Preflight -> NavigationEvent.ToPreflight
                is HolderSessionState.PresentingEngagement -> NavigationEvent.PresentEngagement
                is HolderSessionState.Complete.Failed -> NavigationEvent.ToUnrecoverableError
                else -> null
            }
        }.distinctUntilChanged()
        .flowOn(dispatcher)
        .shareIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Lazily
        )

    init {
        viewModelScope.launch(dispatcher) {
            orchestrator.start()
        }
    }

    sealed interface NavigationEvent {
        data object ToPreflight : NavigationEvent
        data object PresentEngagement : NavigationEvent
        data object ToUnrecoverableError : NavigationEvent
    }
}
