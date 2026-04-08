package uk.gov.onelogin.sharing.verifier.error

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
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

@Inject
@ViewModelKey
@ContributesIntoMap(VerifierUiScope::class)
class UnrecoverableVerifierViewModel(
    private val orchestrator: Orchestrator.Verifier,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    val failureState: StateFlow<VerifierSessionState.Complete.Failed?> =
        orchestrator.verifierSessionState.map {
            it as? VerifierSessionState.Complete.Failed
        }.stateIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Companion.Eagerly,
            orchestrator.verifierSessionState.value as?
                VerifierSessionState.Complete.Failed
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
