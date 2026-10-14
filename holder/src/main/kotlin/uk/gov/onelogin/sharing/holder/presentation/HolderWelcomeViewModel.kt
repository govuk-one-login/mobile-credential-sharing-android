package uk.gov.onelogin.sharing.holder.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.core.engagement.EngagementTimingPolicy
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HolderWelcomeViewModel(
    orchestrator: Orchestrator.Holder,
    @Suppress("UnusedPrivateProperty")
    private val analyticsLogger: AnalyticsLogger,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    timingPolicy: EngagementTimingPolicy = EngagementTimingPolicy()
) : ViewModel() {

    private val qrVisibleDuration: Duration = timingPolicy.qrVisibleDuration

    val uiState: StateFlow<HolderWelcomeUiState> = orchestrator
        .holderSessionState
        .flatMapLatest { sessionState -> sessionState.toUiStates() }
        .distinctUntilChanged()
        .flowOn(dispatcher)
        .stateIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Eagerly,
            HolderWelcomeUiState()
        )

    /**
     * While a QR is being presented, show it for [qrVisibleDuration] then hide it behind a loading
     * spinner. This is purely a display concern: the orchestrator owns the real session deadline
     * and refreshes the session in place, which emits a fresh
     * [HolderSessionState.PresentingEngagement] and restarts this countdown with the new QR.
     */
    private fun HolderSessionState.toUiStates(): Flow<HolderWelcomeUiState> = flow {
        if (this@toUiStates is HolderSessionState.PresentingEngagement) {
            emit(HolderWelcomeUiState(qrData = qrData, showQr = true))
            delay(qrVisibleDuration)
            emit(HolderWelcomeUiState(qrData = qrData, showQr = false))
        } else {
            emit(HolderWelcomeUiState())
        }
    }
}

/**
 * @param qrData The QR payload to render, or `null` when no engagement is being presented.
 * @param showQr `true` to render the QR; `false` to show the loading spinner while the session is
 * about to be refreshed. Only meaningful when [qrData] is non-null.
 */
data class HolderWelcomeUiState(val qrData: String? = null, val showQr: Boolean = false)
