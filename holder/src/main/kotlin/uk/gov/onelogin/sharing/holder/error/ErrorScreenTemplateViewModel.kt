package uk.gov.onelogin.sharing.holder.error

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.buttonEvent
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.errorScreenEvent
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.holder.error.NavigationEvent
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Inject
@ViewModelKey
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
class ErrorScreenTemplateViewModel(
    private val analyticsLogger: AnalyticsLogger,
    private val orchestrator: Orchestrator.Holder
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<NavigationEvent?>()
    val navigationEvent: SharedFlow<NavigationEvent?> = _navigationEvent

    fun reset() = viewModelScope.launch {
        orchestrator.reset()
        _navigationEvent.emit(NavigationEvent.ExitJourney)
    }

    fun sendButtonEvent(name: String) = viewModelScope.launch {
        analyticsLogger.buttonEvent(name)
    }
}
