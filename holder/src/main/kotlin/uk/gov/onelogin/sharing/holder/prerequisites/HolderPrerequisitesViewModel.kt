package uk.gov.onelogin.sharing.holder.prerequisites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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

    init {
        viewModelScope.launch(dispatcher) {
            orchestrator.start()
        }
    }
}
