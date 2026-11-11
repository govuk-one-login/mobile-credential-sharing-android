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
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.holder.presentation.QrCodeScreenAnalytics.qrCodeScreen
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class QrCodeViewModel(
    orchestrator: Orchestrator.Holder,
    private val analyticsLogger: AnalyticsLogger,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val uiState: StateFlow<QrCodeUiState> = orchestrator
        .holderSessionState
        .map { sessionState ->
            (sessionState as? HolderSessionState.PresentingEngagement)
                ?.presentation
                ?.let(::QrCodeUiState)
                ?: QrCodeUiState()
        }
        .distinctUntilChanged()
        .flowOn(dispatcher)
        .stateIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Eagerly,
            QrCodeUiState()
        )

    fun sendScreenEvent(screenName: String) = viewModelScope.launch(dispatcher) {
        analyticsLogger.qrCodeScreen(screenName)
    }
}
