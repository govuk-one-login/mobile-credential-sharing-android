package uk.gov.onelogin.sharing.holder.getreadytoshare

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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.prerequisites.api.MissingPrerequisite
import uk.gov.onelogin.sharing.prerequisites.api.Prerequisite
import uk.gov.onelogin.sharing.prerequisites.api.usecases.ResolvePrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.api.usecases.RetryPrerequisitesNavigator

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class GetReadyToShareViewModel(
    navigator: RetryPrerequisitesNavigator<HolderSessionState>,
    private val analyticsLogger: AnalyticsLogger,
    private val orchestrator: Orchestrator.Holder,
    private val resolver: ResolvePrerequisiteAction<HolderSessionState>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel(),
    ResolvePrerequisiteAction<HolderSessionState> by resolver {

    val navigationEvent: SharedFlow<RetryPrerequisitesNavigator.NavigationEvent> = navigator
        .events
        .map { event ->
            _hasRecheckedPrerequisites.update { false }
            event
        }
        .distinctUntilChanged()
        .flowOn(dispatcher)
        .shareIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Lazily
        )

    private val _hasRecheckedPrerequisites = MutableStateFlow(false)
    val hasRecheckedPrerequisites: StateFlow<Boolean> = _hasRecheckedPrerequisites

    val prerequisites: StateFlow<List<Prerequisite>?> = orchestrator
        .holderSessionState
        .map { it as? HolderSessionState.Preflight }
        .map { it?.map(MissingPrerequisite::prerequisite) }
        .flowOn(dispatcher)
        .stateIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Eagerly,
            (orchestrator.holderSessionState.value as? HolderSessionState.Preflight)
                ?.map(MissingPrerequisite::prerequisite)
        )

    fun recheckPrerequisites(): Job = viewModelScope.launch(dispatcher) {
        _hasRecheckedPrerequisites.update { true }
        (orchestrator.holderSessionState.value as? HolderSessionState.Preflight)
            ?.onComplete()
    }
    
    fun sendScreenEvent() = viewModelScope.launch {
        Screen(
            name = "Get ready to share",
            id = "3f5c1d0b-8068-4e19-8300-37f7bb468ff5",
            params = walletSharingRequiredParameters
        ).let(analyticsLogger::logEventV3Dot1)
    }

    /**
     * DCMAW-23663:
     * - Send button analytics events with [walletSharingRequiredParameters]
     */
    fun onFindOutMore(): Job = viewModelScope.launch(dispatcher) {

    }
}