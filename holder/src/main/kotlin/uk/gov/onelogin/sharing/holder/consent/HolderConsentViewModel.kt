package uk.gov.onelogin.sharing.holder.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class HolderConsentViewModel(orchestrator: Orchestrator.Holder) : ViewModel() {
    val holderSessionState: StateFlow<HolderSessionState> = orchestrator.holderSessionState

    private val _navEvents = MutableSharedFlow<HolderConsentNavEvents>(extraBufferCapacity = 1)
    val navEvents: SharedFlow<HolderConsentNavEvents> = _navEvents

    init {
        viewModelScope.launch {
            orchestrator.holderSessionState.collect { state ->
                if (state is HolderSessionState.Complete.Failed) {
                    _navEvents.tryEmit(HolderConsentNavEvents.NavigateToGenericError)
                }
            }
        }
    }
}
