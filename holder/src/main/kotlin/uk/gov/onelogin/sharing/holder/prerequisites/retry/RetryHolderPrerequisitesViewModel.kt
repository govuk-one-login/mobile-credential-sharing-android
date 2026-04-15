package uk.gov.onelogin.sharing.holder.prerequisites.retry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.ResolvePrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigator

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class RetryHolderPrerequisitesViewModel(
    navigator: RetryPrerequisitesNavigator<HolderSessionState>,
    private val orchestrator: Orchestrator.Holder,
    private val resolver: ResolvePrerequisiteAction<HolderSessionState>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel(),
    ResolvePrerequisiteAction<HolderSessionState> by resolver {

    val navigationEvent: SharedFlow<RetryPrerequisitesNavigator.NavigationEvent?> = navigator
        .events
        .map { event ->
            if (event != null) {
                _hasRecheckedPrerequisites.update { false }
            }
            event
        }
        .shareIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Companion.Lazily
        )

    private val _hasRecheckedPrerequisites = MutableStateFlow(false)
    val hasRecheckedPrerequisites: StateFlow<Boolean> = _hasRecheckedPrerequisites

    val prerequisites: StateFlow<List<Prerequisite>?> = orchestrator
        .holderSessionState
        .map { it as? HolderSessionState.Preflight }
        .map { it?.map(MissingPrerequisiteV2::prerequisite) }
        .stateIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Companion.Eagerly,
            (orchestrator.holderSessionState.value as? HolderSessionState.Preflight)
                ?.map(MissingPrerequisiteV2::prerequisite)
        )

    fun recheckPrerequisites(): Job = viewModelScope.launch(dispatcher) {
        _hasRecheckedPrerequisites.update { true }
        (orchestrator.holderSessionState.value as? HolderSessionState.Preflight)
            ?.onComplete()
    }
}
