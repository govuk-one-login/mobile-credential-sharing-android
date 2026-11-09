package uk.gov.onelogin.sharing.holder.error.generic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorAnalyticsEvents.genericHolderErrorScreen
import uk.gov.onelogin.sharing.orchestration.Orchestrator

@Inject
@ViewModelKey
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
class GenericHolderErrorViewModel(
    private val analyticsLogger: AnalyticsLogger,
    private val orchestrator: Orchestrator.Holder,
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<NavigationEvent?>()
    val navigationEvent: SharedFlow<NavigationEvent?> = _navigationEvent

    fun reset() = viewModelScope.launch {
        orchestrator.reset()
        _navigationEvent.emit(NavigationEvent.ExitJourney)
    }

    fun sendScreenEvent(name: String) = viewModelScope.launch {
        analyticsLogger.genericHolderErrorScreen(name)
    }

    sealed interface NavigationEvent {
        data object ExitJourney : NavigationEvent
    }
}
