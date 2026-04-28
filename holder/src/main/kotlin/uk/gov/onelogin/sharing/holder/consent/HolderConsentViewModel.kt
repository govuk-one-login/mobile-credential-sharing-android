package uk.gov.onelogin.sharing.holder.consent

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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.plus
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class HolderConsentViewModel(
    orchestrator: Orchestrator.Holder,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    val holderSessionState: StateFlow<HolderSessionState> = orchestrator.holderSessionState

    val navEvents: SharedFlow<HolderConsentNavEvents?> = orchestrator
        .holderSessionState
        .map { state ->
            when (state) {
                is HolderSessionState.Complete.Failed ->
                    HolderConsentNavEvents.NavigateToGenericError

                else -> null
            }
        }.flowOn(dispatcher)
        .shareIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Lazily
        )
}
