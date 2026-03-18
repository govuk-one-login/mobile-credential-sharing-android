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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.core.logger.logTag
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

    private val previouslyHadPermissions = MutableStateFlow(
        savedStateHandle[PREVIOUSLY_HAD_PERMISSIONS_KEY] ?: false
    )
    private val hasBluetoothPermissions = MutableStateFlow<Boolean?>(null)

    private val shouldShowErrorScreen = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow("")

    val uiState: StateFlow<HolderWelcomeUiState> = combine(
        orchestrator.holderSessionState,
        shouldShowErrorScreen,
        previouslyHadPermissions,
        errorMessage,
        hasBluetoothPermissions
    ) {
            orchestratorSessionState,
            shouldShowError,
            previouslyHadPermissions,
            errorMessage,
            hasBluetoothPermissions
        ->
        HolderWelcomeUiState(
            qrData = (orchestratorSessionState as? HolderSessionState.PresentingEngagement)?.qrData,
            hasBluetoothPermissions = hasBluetoothPermissions,
            showErrorScreen = shouldShowError,
            errorMessage = errorMessage,
            previouslyHadPermissions = previouslyHadPermissions
        )
    }.stateIn(
        viewModelScope.plus(dispatcher),
        SharingStarted.Eagerly,
        HolderWelcomeUiState(
            previouslyHadPermissions = savedStateHandle[PREVIOUSLY_HAD_PERMISSIONS_KEY] ?: false
        )
    )

    init {
        viewModelScope.launch(dispatcher) {
            orchestrator.holderSessionState.collect { currentSate ->
                if (currentSate is HolderSessionState.Complete.Failed) {
                    errorMessage.update { currentSate.error.message }
                    shouldShowErrorScreen.update { true }
                }
            }
        }
    }

    fun updateBluetoothPermissions(granted: Boolean) {
        val hadPermissionsPreviously = previouslyHadPermissions.value
        val shouldShowError = hadPermissionsPreviously && !granted
        val grantedPermissionsForFirstTime = !hadPermissionsPreviously && granted

        hasBluetoothPermissions.update { granted }
        errorMessage.update {
            if (shouldShowError) {
                "Bluetooth permissions were revoked during the session"
            } else {
                "Bluetooth disconnected"
            }
        }
        previouslyHadPermissions.update { hadPermissionsPreviously || granted }
        shouldShowErrorScreen.update { shouldShowError }

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
}

data class HolderWelcomeUiState(
    val qrData: String? = null,
    val hasBluetoothPermissions: Boolean? = null,
    val showErrorScreen: Boolean = false,
    val errorMessage: String = "",
    val previouslyHadPermissions: Boolean = false
)
