package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied

import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.buttonEvent
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied.DeviceBleDeniedAnalytics.deviceBleDeniedScreen
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.api.usecases.ResolvePrerequisiteAction

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class DeviceBleDeniedViewModel(
    private val analyticsLogger: AnalyticsLogger,
    private val orchestrator: Orchestrator.Holder,
    private val resolver: ResolvePrerequisiteAction<HolderSessionState>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel(),
    ResolvePrerequisiteAction<HolderSessionState> by resolver {

    val isClosing: StateFlow<Boolean> = orchestrator
        .holderSessionState
        .map { it !is HolderSessionState.Preflight }
        .distinctUntilChanged()
        .flowOn(dispatcher)
        .stateIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Eagerly,
            orchestrator.holderSessionState.value !is HolderSessionState.Preflight
        )

    /**
     * Whether the screen has been paused since it last checked. The enable Bluetooth dialogue and
     * the Settings app both pause the screen, so the following resume is the single point where
     * the user may have changed the device's Bluetooth state.
     */
    private val hasBeenPaused = AtomicBoolean(false)

    fun recheckPrerequisites(): Job = viewModelScope.launch(dispatcher) {
        (orchestrator.holderSessionState.value as? HolderSessionState.Preflight)
            ?.onComplete()
    }

    fun onScreenPaused() {
        hasBeenPaused.set(true)
    }

    /**
     * Rechecks prerequisites only when returning to the screen. Skips the initial resume, as the
     * previous screen has only just performed the checks that routed here.
     */
    fun onScreenResumed(): Job? = if (hasBeenPaused.getAndSet(false)) {
        recheckPrerequisites()
    } else {
        null
    }

    fun turnOnBluetooth(launcher: ActivityResultLauncher<PrerequisiteAction>) {
        resolve(launcher)
    }

    fun exitJourney(): Job = viewModelScope.launch(dispatcher) {
        orchestrator.cancel()
    }

    fun sendScreenEvent(screenName: String) = viewModelScope.launch(dispatcher) {
        analyticsLogger.deviceBleDeniedScreen(screenName = screenName)
    }

    fun sendTurnOnBluetoothEvent(buttonText: String) = viewModelScope.launch(dispatcher) {
        analyticsLogger.buttonEvent(text = buttonText)
    }

    fun sendCancelEvent(buttonText: String) = viewModelScope.launch(dispatcher) {
        analyticsLogger.buttonEvent(text = buttonText)
    }
}
