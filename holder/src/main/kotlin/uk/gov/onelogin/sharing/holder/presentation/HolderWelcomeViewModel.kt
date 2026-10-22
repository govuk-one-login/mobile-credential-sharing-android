package uk.gov.onelogin.sharing.holder.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class HolderWelcomeViewModel(
    orchestrator: Orchestrator.Holder,
    @Suppress("UnusedPrivateProperty")
    private val analyticsLogger: AnalyticsLogger,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val uiState: StateFlow<HolderWelcomeUiState> = orchestrator
        .holderSessionState
        .map { sessionState ->
            HolderWelcomeUiState(
                qrData = (sessionState as? HolderSessionState.PresentingEngagement)?.qrData
            )
        }.distinctUntilChanged()
        .flowOn(dispatcher)
        .stateIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Eagerly,
            HolderWelcomeUiState()
        )

    fun sendScreenEvent() = viewModelScope.launch(dispatcher) {
        ViewEvent.Screen(
            name = "HolderWelcomeScreen",
            id = "29fd14a3-4390-4939-9d57-d9fe8cf61811",
            params = walletSharingRequiredParameters
        ).let(analyticsLogger::logEventV3Dot1)
    }
}

data class HolderWelcomeUiState(val qrData: String? = null)
