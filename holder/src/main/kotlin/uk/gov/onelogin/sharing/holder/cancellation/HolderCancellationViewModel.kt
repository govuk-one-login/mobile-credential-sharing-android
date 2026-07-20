package uk.gov.onelogin.sharing.holder.cancellation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class HolderCancellationViewModel(
    private val orchestrator: Orchestrator.Holder,
    private val ioDispatcher: CoroutineContext = Dispatchers.IO
) : ViewModel() {

    fun cancelJourney(): Job = viewModelScope.launch(ioDispatcher) {
        orchestrator.cancel()
    }
}