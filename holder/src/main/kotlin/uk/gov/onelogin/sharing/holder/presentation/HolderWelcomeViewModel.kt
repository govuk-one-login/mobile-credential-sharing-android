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
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.gov.logging.api.Logger
import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.bluetooth.BluetoothUiErrorTypes
import uk.gov.onelogin.sharing.bluetooth.BluetoothUiErrorTypes.BLUETOOTH_DISCONNECTED
import uk.gov.onelogin.sharing.bluetooth.BluetoothUiErrorTypes.PERMISSIONS_MISSING
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.core.implementation.RequiresImplementation
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.holder.mdoc.MdocSessionError
import uk.gov.onelogin.sharing.holder.mdoc.MdocSessionManager
import uk.gov.onelogin.sharing.holder.mdoc.MdocSessionState
import uk.gov.onelogin.sharing.holder.mdoc.SessionManagerFactory
import uk.gov.onelogin.sharing.security.engagement.Engagement
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity

@AssistedInject
@Suppress("LongParameterList")
class HolderWelcomeViewModel(
    private val sessionSecurity: SessionSecurity,
    private val engagementGenerator: Engagement,
    mdocSessionManagerFactory: SessionManagerFactory,
    private val logger: Logger,
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val resettable: Set<Resettable>,
    private val orchestrator: Orchestrator,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    companion object {
        private const val PREVIOUSLY_HAD_PERMISSIONS_KEY = "previouslyHadPermissions"
    }

    private val initialState = HolderWelcomeUiState(
        previouslyHadPermissions = savedStateHandle[PREVIOUSLY_HAD_PERMISSIONS_KEY] ?: false
    )

    private val _uiState = MutableStateFlow(initialState)
    private val mdocBleSession: MdocSessionManager =
        mdocSessionManagerFactory.create(viewModelScope)

    private var sessionStartRequested = false
    val uiState: StateFlow<HolderWelcomeUiState> = _uiState

    init {
        viewModelScope.launch(dispatcher) {
            resettable.forEach(Resettable::reset)
            val publicKey = sessionSecurity.generateSessionPublicKey()
            publicKey.let { coseKey ->
                val engagement = engagementGenerator.qrCodeEngagement(
                    coseKey,
                    _uiState.value.uuid
                )
                _uiState.update { it.copy(qrData = "${Engagement.QR_CODE_SCHEME}$engagement") }
            }

            // this doesn't do anything at the moment
            orchestrator.start()
        }

        viewModelScope.launch {
            mdocBleSession.state.collect { state ->
                _uiState.update { it.copy(sessionState = state) }

                when (state) {
                    MdocSessionState.AdvertisingStarted ->
                        logger.debug(
                            logTag,
                            "Mdoc - Advertising Started UUID: ${_uiState.value.uuid}"
                        )

                    MdocSessionState.AdvertisingStopped -> {
                        sessionStartRequested = false
                        logger.debug(logTag, "Mdoc - Advertising Stopped")
                    }

                    is MdocSessionState.Connected ->
                        logger.debug(logTag, "Mdoc - Connected: ${state.address}")

                    is MdocSessionState.Disconnected -> {
                        @RequiresImplementation(
                            details = [
                                ImplementationDetail(
                                    ticket = "DCMAW-16898",
                                    description = "We may need to handle explicit bluetooth" +
                                        "disconnection states to handle common error codes " +
                                        "8, 19, 22 and 133. The function below will handle " +
                                        "treat all disconnect states the same when connected " +
                                        "to a device"
                                )
                            ]
                        )
                        logger.debug(logTag, "Error Mdoc - Disconnected: ${state.address}")
                        _uiState.update {
                            it.copy(
                                connectedAddress = state.address,
                                showErrorScreen = true,
                                bluetoothErrorType = BLUETOOTH_DISCONNECTED
                            )
                        }
                        stopAdvertising()
                    }

                    is MdocSessionState.Error -> {
                        sessionStartRequested = false
                        handleError(state.reason)
                    }

                    MdocSessionState.GattServiceStopped -> {
                        sessionStartRequested = false
                        logger.debug(logTag, "Mdoc - GattService Stopped")
                    }

                    MdocSessionState.Idle -> {
                        sessionStartRequested = false
                        logger.debug(logTag, "Mdoc - Idle")
                    }

                    is MdocSessionState.ServiceAdded ->
                        logger.debug(logTag, "Mdoc - Service Added: ${state.uuid}")
                }
            }
        }

        viewModelScope.launch {
            mdocBleSession.bluetoothStatus.collect { bluetoothState ->
                when (bluetoothState) {
                    BluetoothStatus.OFF,
                    BluetoothStatus.TURNING_OFF -> {
                        val wasDisabled = _uiState.value.bluetoothState == BluetoothState.Disabled
                        if (!wasDisabled) {
                            logger.debug(logTag, "Mdoc - Bluetooth switched OFF")
                            _uiState.update {
                                it.copy(
                                    showEnableBluetoothPrompt = true,
                                    bluetoothState = BluetoothState.Disabled
                                )
                            }
                        }
                        stopAdvertising()
                    }

                    BluetoothStatus.TURNING_ON -> {
                        logger.debug(logTag, "Mdoc - Bluetooth initializing")
                        _uiState.update {
                            it.copy(
                                showEnableBluetoothPrompt = false,
                                bluetoothState = BluetoothState.Initializing,
                                showErrorScreen = false
                            )
                        }
                    }

                    BluetoothStatus.ON -> {
                        logger.debug(logTag, "Mdoc - Bluetooth switched ON")
                        _uiState.update {
                            it.copy(
                                showEnableBluetoothPrompt = false,
                                bluetoothState = BluetoothState.Enabled,
                                showErrorScreen = false
                            )
                        }
                        startBleSession()
                    }

                    BluetoothStatus.UNKNOWN ->
                        logger.debug(logTag, "Mdoc - Bluetooth status unknown")
                }
            }
        }
    }

    private fun handleError(reason: MdocSessionError) {
        when (reason) {
            MdocSessionError.ADVERTISING_FAILED ->
                logger.debug(logTag, "Mdoc - Error: Advertising failed")

            MdocSessionError.GATT_NOT_AVAILABLE ->
                logger.debug(logTag, "Mdoc - Error: GATT not available")

            MdocSessionError.BLUETOOTH_PERMISSION_MISSING ->
                logger.debug(logTag, "Mdoc - Error: Bluetooth permission missing")

            MdocSessionError.DESCRIPTOR_WRITE_REQUEST_FAILED ->
                logger.debug(logTag, "Mdoc - Error: Descriptor write request failed")
        }
    }

    fun stopAdvertising() {
        viewModelScope.launch {
            mdocBleSession.stop()
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
                bluetoothErrorType = if (shouldShowError) {
                    PERMISSIONS_MISSING
                } else {
                    BLUETOOTH_DISCONNECTED
                }
            )
        }

        if (shouldShowError) {
            logger.debug(logTag, "Error - Permissions were revoked during the session")
            stopAdvertising()
        }

        if (grantedPermissionsForFirstTime) {
            savedStateHandle[PREVIOUSLY_HAD_PERMISSIONS_KEY] = true
        }

        if (granted) {
            startBleSession()
        }
    }

    private fun startBleSession() {
        val state = _uiState.value

        val hasPermissions = state.hasBluetoothPermissions == true
        val bluetoothOn = state.bluetoothState == BluetoothState.Enabled

        val canStart = !sessionStartRequested &&
            hasPermissions &&
            bluetoothOn &&
            canStartNewSession(state) &&
            !sessionStartRequested

        if (canStart) {
            sessionStartRequested = true
            viewModelScope.launch {
                mdocBleSession.start(state.uuid)
            }
        }
    }

    private fun canStartNewSession(state: HolderWelcomeUiState): Boolean =
        state.sessionState == MdocSessionState.Idle ||
            state.sessionState == MdocSessionState.AdvertisingStopped ||
            state.sessionState == MdocSessionState.GattServiceStopped

    @AssistedFactory
    @ViewModelAssistedFactoryKey(HolderWelcomeViewModel::class)
    @ContributesIntoMap(ViewModelScope::class)
    interface Factory : ViewModelAssistedFactory {
        fun create(@Assisted savedStateHandle: SavedStateHandle): HolderWelcomeViewModel
        override fun create(extras: CreationExtras): HolderWelcomeViewModel {
            val savedStateHandle = extras.createSavedStateHandle()
            return create(savedStateHandle)
        }
    }

    fun onScreenDisposed() {
        if (_uiState.value.sessionState is MdocSessionState.Connected) {
            logger.debug(logTag, "Holder stopped advertising during session")
        }
        stopAdvertising()
    }

    override fun onCleared() {
        viewModelScope.launch {
            mdocBleSession.stop()
            resettable.forEach(Resettable::reset)
        }
        super.onCleared()
    }
}

data class HolderWelcomeUiState(
    val uuid: UUID = UUID.randomUUID(),
    val qrData: String? = null,
    val sessionState: MdocSessionState = MdocSessionState.Idle,
    val lastErrorMessage: String? = null,
    val bluetoothState: BluetoothState = BluetoothState.Unknown,
    val hasBluetoothPermissions: Boolean? = null,
    val showErrorScreen: Boolean = false,
    val bluetoothErrorType: BluetoothUiErrorTypes = BLUETOOTH_DISCONNECTED,
    val previouslyHadPermissions: Boolean = false,
    val showEnableBluetoothPrompt: Boolean = false,
    val connectedAddress: String? = ""
)
