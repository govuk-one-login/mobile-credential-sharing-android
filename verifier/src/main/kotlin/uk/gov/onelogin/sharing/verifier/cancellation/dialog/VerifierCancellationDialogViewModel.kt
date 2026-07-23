package uk.gov.onelogin.sharing.verifier.cancellation.dialog

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
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator

@Inject
@ContributesIntoMap(VerifierUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class VerifierCancellationDialogViewModel(
    private val orchestrator: Orchestrator.Verifier,
    private val ioDispatcher: CoroutineContext = Dispatchers.IO
) : ViewModel() {

    fun cancelJourney(): Job = viewModelScope.launch(ioDispatcher) {
        orchestrator.cancel()
    }
}
