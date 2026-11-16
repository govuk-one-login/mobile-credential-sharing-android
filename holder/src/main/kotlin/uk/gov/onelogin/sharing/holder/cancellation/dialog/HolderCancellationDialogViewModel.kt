package uk.gov.onelogin.sharing.holder.cancellation.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.holder.cancellation.dialog.HolderCancellationDialogAnalyticsEvents.continueWithTheCheckButton
import uk.gov.onelogin.sharing.holder.cancellation.dialog.HolderCancellationDialogAnalyticsEvents.stopTheCheckButton
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class HolderCancellationDialogViewModel(
    private val orchestrator: Orchestrator.Holder,
    private val analyticsLogger: AnalyticsLogger,
    private val ioDispatcher: CoroutineContext = Dispatchers.IO
) : ViewModel() {

    /**
     * Stops the check.
     *
     * From the "Agree to share" screen this runs the existing Deny behaviour, so the Verifier is
     * sent an empty, status 0 DeviceResponse followed by the session termination. Before a request
     * has been received (for example "Getting list of details") there is nothing to deny, so the
     * journey is cancelled instead.
     */
    fun stopTheCheck(): Job = viewModelScope.launch(ioDispatcher) {
        analyticsLogger.stopTheCheckButton()
        if (orchestrator.holderSessionState.value is HolderSessionState.AwaitingUserConsent) {
            orchestrator.denyConsent()
        } else {
            orchestrator.cancel()
        }
    }

    fun continueWithTheCheck(): Job = viewModelScope.launch(ioDispatcher) {
        analyticsLogger.continueWithTheCheckButton()
    }
}
