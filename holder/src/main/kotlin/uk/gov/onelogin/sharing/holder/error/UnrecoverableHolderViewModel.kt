package uk.gov.onelogin.sharing.holder.error

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Inject
@ViewModelKey
@ContributesIntoMap(HolderUiScope::class)
class UnrecoverableHolderViewModel(
    private val orchestrator: Orchestrator.Holder,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    val failureState: StateFlow<HolderSessionState.Complete.Failed?> =
        orchestrator.holderSessionState.map {
            it as? HolderSessionState.Complete.Failed
        }.stateIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Companion.Eagerly,
            orchestrator.holderSessionState.value as?
                HolderSessionState.Complete.Failed
        )

    private val _navigationEvent = MutableSharedFlow<NavigationEvent?>()
    val navigationEvent: SharedFlow<NavigationEvent?> = _navigationEvent

    fun exitJourney() = viewModelScope.launch(dispatcher) {
        orchestrator.reset()
        _navigationEvent.emit(NavigationEvent.ExitJourney)
    }

    sealed interface NavigationEvent {
        data object ExitJourney : NavigationEvent
    }
}
