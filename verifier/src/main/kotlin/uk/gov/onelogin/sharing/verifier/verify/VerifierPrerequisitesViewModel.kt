package uk.gov.onelogin.sharing.verifier.verify

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.core.presentation.permissions.isPermanentlyDenied
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

@Inject
@ViewModelKey
@ContributesIntoMap(VerifierUiScope::class)
@OptIn(ExperimentalPermissionsApi::class)
class VerifierPrerequisitesViewModel(
    private val logger: Logger,
    private val bluetoothStateMonitor: BluetoothStateMonitor,
    orchestrator: Orchestrator.Verifier,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {
    private val allGranted = MutableStateFlow<Boolean?>(null)
    private val bluetoothStatus = MutableStateFlow<BluetoothStatus?>(null)
    private val hasPreviouslyRequestedPermission = MutableStateFlow(false)

    private val preconditionsState: Flow<VerifyCredentialPreconditionsState> = combine(
        allGranted,
        bluetoothStatus
    ) { granted, bluetooth ->
        when {
            granted == null ->
                VerifyCredentialPreconditionsState.Idle

            !granted ->
                VerifyCredentialPreconditionsState.BluetoothAccessDenied

            bluetooth == null ->
                VerifyCredentialPreconditionsState.Idle

            bluetooth == BluetoothStatus.ON ->
                VerifyCredentialPreconditionsState.Met

            else ->
                VerifyCredentialPreconditionsState.BluetoothDisabled
        }.also {
            logger.debug(
                logTag,
                "Updated precondition state to $it"
            )
        }
    }

    val uiState: StateFlow<VerifyCredentialUiState> = combine(
        hasPreviouslyRequestedPermission,
        preconditionsState
    ) { hasRequestedPermission, preconditions ->
        VerifyCredentialUiState(
            hasPreviouslyRequestedPermission = hasRequestedPermission,
            preconditionsState = preconditions
        )
    }.stateIn(
        viewModelScope.plus(dispatcher),
        SharingStarted.Eagerly,
        VerifyCredentialUiState()
    )

    val events: StateFlow<VerifyCredentialEvents?> = combine(
        orchestrator.verifierSessionState,
        preconditionsState
    ) { session, preconditions ->
        when {
            session is VerifierSessionState.Preflight ->
                VerifyCredentialEvents.NavigateToPreflight

            session is VerifierSessionState.ReadyToScan &&
                preconditions is VerifyCredentialPreconditionsState.Met ->
                VerifyCredentialEvents.NavigateToScanner

            else -> null
        }
    }.stateIn(
        viewModelScope.plus(dispatcher),
        SharingStarted.Eagerly,
        null
    )

    init {
        bluetoothStateMonitor.start()
        orchestrator.start()

        viewModelScope.launch(dispatcher) {
            bluetoothStateMonitor.states
                .distinctUntilChanged()
                .collect { status ->
                    bluetoothStatus.update { status }
                    logger.debug(logTag, "Bluetooth status: $status")
                }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onCleared() {
        bluetoothStateMonitor.stop()
        super.onCleared()
    }

    fun onPermissionRequestLaunched() {
        hasPreviouslyRequestedPermission.update { true }
    }

    fun onPermissionsChanged(permissionsState: MultiplePermissionsState) {
        logPermissions(permissionsState)
        allGranted.update { permissionsState.allPermissionsGranted }
    }

    private fun logPermissions(permissionsState: MultiplePermissionsState) {
        when {
            permissionsState.allPermissionsGranted -> logger.debug(
                logTag,
                "All required Bluetooth permissions have been granted"
            )

            permissionsState.isPermanentlyDenied() -> logger.debug(
                logTag,
                "Bluetooth permissions were permanently denied"
            )

            else -> {
                logger.debug(logTag, "Bluetooth permissions were denied")
            }
        }
    }
}

data class VerifyCredentialUiState(
    val hasPreviouslyRequestedPermission: Boolean = false,
    val preconditionsState: VerifyCredentialPreconditionsState =
        VerifyCredentialPreconditionsState.Idle
)

sealed interface VerifyCredentialEvents {
    data object NavigateToScanner : VerifyCredentialEvents
    data object NavigateToPreflight : VerifyCredentialEvents
}
