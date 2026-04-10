@file:OptIn(ExperimentalPermissionsApi::class)

package uk.gov.onelogin.sharing.verifier.connect

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.Receiver
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.core.implementation.RequiresImplementation
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.core.presentation.permissions.isPermanentlyDenied
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState as OrchestratorVerifierSessionState
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEvent.RequestedPermission
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEvent.UpdatePermission

@Suppress("LongParameterList")
@AssistedInject
class SessionEstablishmentViewModel(
    private val logger: Logger,
    private val verifierOrchestrator: Orchestrator.Verifier,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel(),
    Receiver<ConnectWithHolderDeviceEvent> {
    private val initialState = ConnectWithHolderDeviceState(
        previouslyHadPermissions = savedStateHandle[PREVIOUSLY_HAD_PERMISSIONS_KEY] ?: false
    )
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<ConnectWithHolderDeviceState> = _uiState

    private val _navEvents = MutableSharedFlow<ConnectWithHolderDeviceNavEvent>(
        extraBufferCapacity = 1
    )
    val navEvents: SharedFlow<ConnectWithHolderDeviceNavEvent> = _navEvents

    @AssistedFactory
    @ViewModelAssistedFactoryKey(SessionEstablishmentViewModel::class)
    @ContributesIntoMap(VerifierUiScope::class)
    interface Factory : ViewModelAssistedFactory {
        fun create(@Assisted savedStateHandle: SavedStateHandle): SessionEstablishmentViewModel

        override fun create(extras: CreationExtras): SessionEstablishmentViewModel {
            val savedStateHandle = extras.createSavedStateHandle()
            return create(savedStateHandle)
        }
    }

    init {
        viewModelScope.launch {
            @RequiresImplementation(
                details = [
                    ImplementationDetail(
                        ticket = "NA",
                        description = "Need to replace the disconnect scenarios. State from " +
                            "orchestrator to be observed instead of the now removed " +
                            "mdocVerifierSessionState"
                    )
                ]
            )
            verifierOrchestrator.verifierSessionState.collect { sessionState ->
                updateState {
                    it.copy(
                        isLoading = sessionState is OrchestratorVerifierSessionState.Connecting
                    )
                }
                when (sessionState) {
                    is OrchestratorVerifierSessionState.Complete.Failed -> {
                        _navEvents.tryEmit(
                            ConnectWithHolderDeviceNavEvent.NavigateToError(
                                BluetoothSessionError.BluetoothConnectionError
                            )
                        )
                    }

                    else -> Unit
                }
            }
        }
    }

    /**
     * @see updateHasRequestPermissions
     * @see updatePermissions
     */
    @OptIn(ExperimentalPermissionsApi::class)
    override fun receive(event: ConnectWithHolderDeviceEvent) = when (event) {
        is RequestedPermission ->
            updateHasRequestPermissions(event.hasRequestedPermission)

        is UpdatePermission ->
            updatePermissions(event.state)

        else -> Unit
    }

    override fun onCleared() {
        logger.debug(logTag, "VM cleared")
        super.onCleared()
    }

    private fun updateHasRequestPermissions(requestedPerms: Boolean) {
        updateState {
            it.copy(
                hasRequestedPermissions = requestedPerms
            )
        }
    }

    private fun updatePermissions(state: MultiplePermissionsState) {
        val hadPermissionsPreviously = _uiState.value.previouslyHadPermissions
        val hasAllPerms = state.allPermissionsGranted
        val shouldShowError = hadPermissionsPreviously && !hasAllPerms
        val grantedPermissionsForFirstTime = !hadPermissionsPreviously && hasAllPerms

        updateState {
            it.copy(hasAllPermissions = hasAllPerms)
        }
        when {
            hasAllPerms -> "All required Bluetooth permissions have been granted"
            state.isPermanentlyDenied() -> "Bluetooth permissions were permanently denied"
            else -> "Bluetooth permissions were denied"
        }.let { logger.debug(logTag, it) }

        if (grantedPermissionsForFirstTime) {
            savedStateHandle[PREVIOUSLY_HAD_PERMISSIONS_KEY] = true
        }

        if (shouldShowError) {
            logger.debug(logTag, "Bluetooth app permissions revoked during session")
            _navEvents.tryEmit(
                ConnectWithHolderDeviceNavEvent.NavigateToError(
                    BluetoothSessionError.BluetoothPermissionsError
                )
            )
        }
    }

    private fun updateState(
        updatedState: (ConnectWithHolderDeviceState) -> ConnectWithHolderDeviceState
    ) {
        _uiState.update(updatedState)
    }

    companion object {
        const val PREVIOUSLY_HAD_PERMISSIONS_KEY = "previouslyHadPermissions"
    }
}
