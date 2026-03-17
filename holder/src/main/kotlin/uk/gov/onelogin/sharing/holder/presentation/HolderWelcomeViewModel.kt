package uk.gov.onelogin.sharing.holder.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.core.implementation.RequiresImplementation
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@AssistedInject
@Suppress("LongParameterList")
class HolderWelcomeViewModel(
    private val logger: Logger,
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val orchestrator: Orchestrator.Holder,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    companion object {
        private const val PREVIOUSLY_HAD_PERMISSIONS_KEY = "previouslyHadPermissions"
    }

    private val initialState = HolderWelcomeUiState(
        previouslyHadPermissions = savedStateHandle[PREVIOUSLY_HAD_PERMISSIONS_KEY] ?: false
    )

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<HolderWelcomeUiState> = _uiState

    init {
        viewModelScope.launch(dispatcher) {
            orchestrator.start()

            orchestrator.holderSessionState.collect { currentSate ->
                when (currentSate) {
                    is HolderSessionState.PresentingEngagement -> _uiState.update {
                        it.copy(qrData = currentSate.qrData)
                    }

                    is HolderSessionState.Complete.Failed -> _uiState.update {
                        it.copy(
                            showErrorScreen = true,
                            errorMessage = currentSate.error.message
                        )
                    }

                    else -> Unit
                }
            }
        }
    }

    fun updateBluetoothPermissions(granted: Boolean) {
        val hadPermissionsPreviously = _uiState.value.previouslyHadPermissions
        val shouldShowError = hadPermissionsPreviously && !granted
        val grantedPermissionsForFirstTime = !hadPermissionsPreviously && granted

        _uiState.update { state ->
            state.copy(
                hasBluetoothPermissions = granted,
                previouslyHadPermissions = hadPermissionsPreviously || granted,
                showErrorScreen = shouldShowError,
                errorMessage = if (shouldShowError) {
                    "Bluetooth permissions were revoked during the session"
                } else {
                    "Bluetooth disconnected"
                }
            )
        }

        if (shouldShowError) {
            logger.debug(logTag, "Error - Permissions were revoked during the session")
        }

        if (grantedPermissionsForFirstTime) {
            savedStateHandle[PREVIOUSLY_HAD_PERMISSIONS_KEY] = true
        }
    }

    @AssistedFactory
    @ViewModelAssistedFactoryKey(HolderWelcomeViewModel::class)
    @ContributesIntoMap(HolderUiScope::class)
    interface Factory : ViewModelAssistedFactory {
        fun create(@Assisted savedStateHandle: SavedStateHandle): HolderWelcomeViewModel
        override fun create(extras: CreationExtras): HolderWelcomeViewModel {
            val savedStateHandle = extras.createSavedStateHandle()
            return create(savedStateHandle)
        }
    }

    fun onScreenDisposed() {
        @RequiresImplementation(
            details = [
                ImplementationDetail(
                    ticket = "UI",
                    description = "We don't have an explicit back button or way to test this atm" +
                        "so will send end command onNavBack to test closing the connection" +
                        "for now."
                )
            ]
        )
        orchestrator.cancel()
        logger.debug(logTag, "Holder stopped advertising during session")
    }
}

data class HolderWelcomeUiState(
    val qrData: String? = null,
    val bluetoothState: BluetoothState = BluetoothState.Unknown,
    val hasBluetoothPermissions: Boolean? = null,
    val showErrorScreen: Boolean = false,
    val errorMessage: String = "",
    val previouslyHadPermissions: Boolean = false,
    val showEnableBluetoothPrompt: Boolean = false,
    val deviceRequest: DeviceRequest? = null
)
