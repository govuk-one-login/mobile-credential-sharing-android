package uk.gov.onelogin.sharing.holder.error.readerauth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.launch
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.holder.error.readerauth.ReaderAuthErrorAnalyticsEvents.readerAuthErrorScreen
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Inject
@ViewModelKey
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
class ReaderAuthErrorViewModel(
    private val analyticsLogger: AnalyticsLogger,
    private val orchestrator: Orchestrator.Holder
) : ViewModel() {

    fun sendScreenEvent(name: String) = viewModelScope.launch {
        val reason = (orchestrator.holderSessionState.value as? HolderSessionState.Complete.Failed)
            ?.reason ?: ""
        analyticsLogger.readerAuthErrorScreen(name, reason)
    }
}
