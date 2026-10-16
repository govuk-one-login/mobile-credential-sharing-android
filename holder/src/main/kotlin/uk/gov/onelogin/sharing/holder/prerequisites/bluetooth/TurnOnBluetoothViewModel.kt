package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
import uk.gov.logging.api.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.TrackEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.prerequisites.api.Prerequisite
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.api.usecases.ResolvePrerequisiteAction

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class TurnOnBluetoothViewModel(
    private val analyticsLogger: AnalyticsLogger,
    private val orchestrator: Orchestrator.Holder,
    private val resolver: ResolvePrerequisiteAction<HolderSessionState>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel(),
    ResolvePrerequisiteAction<HolderSessionState> by resolver {

    private val _hasRecheckedPrerequisites = MutableStateFlow(false)
    val hasRecheckedPrerequisites: StateFlow<Boolean> = _hasRecheckedPrerequisites

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

    fun recheckPrerequisites(): Job = viewModelScope.launch(dispatcher) {
        _hasRecheckedPrerequisites.update { true }
        (orchestrator.holderSessionState.value as? HolderSessionState.Preflight)
            ?.onComplete()
    }

    fun turnOnBluetooth(launcher: ActivityResultLauncher<PrerequisiteAction>) {
        sendTurnOnBluetoothEvent(AnalyticsButton.TurnOnBluetooth)
        resolve(launcher)
    }

    fun sendScreenEvent() = viewModelScope.launch(dispatcher) {
        ViewEvent.Screen(
            name = "Turn on Bluetooth",
            id = "TurnOnBluetoothScreen",
            params = walletSharingRequiredParameters
        ).let(analyticsLogger::logEventV3Dot1)
    }

    fun sendTurnOnBluetoothEvent(button: AnalyticsButton) =
        viewModelScope.launch(dispatcher) {
            TrackEvent.Button(
                text = button.name,
                params = walletSharingRequiredParameters
            ).let(analyticsLogger::logEventV3Dot1)
    }

    sealed interface AnalyticsButton {
        val name: String

        data object TurnOnBluetooth : AnalyticsButton {
            override val name: String get() = "Turn on Bluetooth"
        }
    }
}
