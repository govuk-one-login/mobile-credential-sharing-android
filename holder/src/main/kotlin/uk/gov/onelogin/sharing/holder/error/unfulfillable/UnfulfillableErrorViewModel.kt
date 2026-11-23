package uk.gov.onelogin.sharing.holder.error.unfulfillable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.launch
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.errorScreenEvent
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Inject
@ViewModelKey
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
class UnfulfillableErrorViewModel(
    private val analyticsLogger: AnalyticsLogger
) : ViewModel() {
    fun sendScreenEvent(name: String) = viewModelScope.launch {
        analyticsLogger.errorScreenEvent(
            name = name,
            id = "1c5c61b3-b8d4-4355-9be3-b26440a43b3f",
            reason = HolderSessionState.Complete.SuccessReason.UnfulfillableRequest.toString(),
        )
    }
}