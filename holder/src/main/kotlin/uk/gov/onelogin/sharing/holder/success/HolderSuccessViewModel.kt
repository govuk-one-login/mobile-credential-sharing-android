package uk.gov.onelogin.sharing.holder.success

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.orchestration.Orchestrator

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class HolderSuccessViewModel(
    val orchestrator: Orchestrator.Holder,
    val dispatcher: CoroutineContext = Dispatchers.IO
) : ViewModel(),
    Resettable {
    override fun reset() {
        viewModelScope.launch { orchestrator.reset() }
    }
}
