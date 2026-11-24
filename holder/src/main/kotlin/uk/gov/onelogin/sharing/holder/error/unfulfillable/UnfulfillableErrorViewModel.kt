package uk.gov.onelogin.sharing.holder.error.unfulfillable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.holder.error.unfulfillable.UnfulfillableErrorAnalyticsEvents.unfulfillableErrorScreen
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Inject
@ViewModelKey
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
class UnfulfillableErrorViewModel(
    private val analyticsLogger: AnalyticsLogger,
    orchestrator: Orchestrator.Holder,
) : ViewModel() {

    private val successReason = orchestrator.holderSessionState
        .filterIsInstance<HolderSessionState.Complete.Success>()
        .map { it.successReason }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            HolderSessionState.Complete.SuccessReason.UnfulfillableRequest
        )

    fun sendScreenEvent(name: String) = viewModelScope.launch {
        analyticsLogger.unfulfillableErrorScreen(
            name = name,
            reason = successReason.value,
        )
    }
}
