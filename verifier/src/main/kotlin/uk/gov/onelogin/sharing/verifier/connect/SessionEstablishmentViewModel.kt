@file:OptIn(ExperimentalPermissionsApi::class)

package uk.gov.onelogin.sharing.verifier.connect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.advertising.BleAdvertiser
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

@Inject
@ViewModelKey
@ContributesIntoMap(VerifierUiScope::class)
class SessionEstablishmentViewModel(
    private val logger: Logger,
    private val verifierOrchestrator: Orchestrator.Verifier,
    private val advertiser: BleAdvertiser,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    val uiState: StateFlow<ConnectWithHolderDeviceState> = verifierOrchestrator
        .verifierSessionState
        .map { state ->
            ConnectWithHolderDeviceState(
                isBluetoothEnabled = advertiser.isBluetoothEnabled(),
                isLoading = state is VerifierSessionState.Connecting
            )
        }.distinctUntilChanged()
        .flowOn(dispatcher)
        .stateIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Eagerly,
            ConnectWithHolderDeviceState(
                isBluetoothEnabled = advertiser.isBluetoothEnabled(),
                isLoading = true
            )
        )

    val navEvents: SharedFlow<ConnectWithHolderDeviceNavEvent> = verifierOrchestrator
        .verifierSessionState
        .mapNotNull { state ->
            when (state) {
                is VerifierSessionState.Complete.Failed ->
                    ConnectWithHolderDeviceNavEvent.NavigateToError(
                        BluetoothSessionError.BluetoothConnectionError
                    )

                else -> null
            }
        }.distinctUntilChanged()
        .flowOn(dispatcher)
        .shareIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Lazily
        )

    override fun onCleared() {
        logger.debug(logTag, "VM cleared")
        super.onCleared()
    }
}
