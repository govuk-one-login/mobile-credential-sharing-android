package uk.gov.onelogin.sharing.holder.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionError
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionState
import uk.gov.onelogin.sharing.bluetooth.api.SessionManagerFactory
import uk.gov.onelogin.sharing.bluetooth.internal.core.BluetoothStatus
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.engagement.Engagement
import uk.gov.onelogin.sharing.security.engagement.EngagementAlgorithms.EC_ALGORITHM
import uk.gov.onelogin.sharing.security.engagement.EngagementAlgorithms.EC_PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity

@Inject
@ViewModelKey(HolderWelcomeViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class HolderWelcomeViewModel(
    private val sessionSecurity: SessionSecurity,
    private val engagementGenerator: Engagement,
    mdocSessionManagerFactory: SessionManagerFactory,
    private val logger: Logger,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val initialState = HolderWelcomeUiState()
    private val _uiState = MutableStateFlow(initialState)
    private val mdocBleSession: MdocSessionManager =
        mdocSessionManagerFactory.create(viewModelScope)

    private var sessionStartRequested = false
    val uiState: StateFlow<HolderWelcomeUiState> = _uiState

    init {
        viewModelScope.launch(dispatcher) {
            val pubKey = sessionSecurity.generateEcPublicKey(EC_ALGORITHM, EC_PARAMETER_SPEC)
            val key = pubKey?.let { CoseKey.generateCoseKey(it) }
            if (key != null) {
                val engagement = engagementGenerator.qrCodeEngagement(key, _uiState.value.uuid)
                _uiState.update { it.copy(qrData = "${Engagement.QR_CODE_SCHEME}$engagement") }
            }
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

                    is MdocSessionState.Disconnected ->
                        logger.debug(logTag, "Mdoc - Disconnected: ${state.address}")

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
                                it.copy(bluetoothState = BluetoothState.Disabled)
                            }
                        }
                    }

                    BluetoothStatus.TURNING_ON -> {
                        logger.debug(logTag, "Mdoc - Bluetooth initializing")
                        _uiState.update {
                            it.copy(bluetoothState = BluetoothState.Initializing)
                        }
                    }

                    BluetoothStatus.ON -> {
                        logger.debug(logTag, "Mdoc - Bluetooth switched ON")
                        _uiState.update { it.copy(bluetoothState = BluetoothState.Enabled) }
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
        }
    }

    fun stopAdvertising() {
        viewModelScope.launch {
            mdocBleSession.stop()
        }
    }

    fun updateBluetoothPermissions(state: Boolean) {
        _uiState.update {
            it.copy(hasBluetoothPermissions = state)
        }

        startBleSession()
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
}

data class HolderWelcomeUiState(
    val uuid: UUID = UUID.randomUUID(),
    val qrData: String? = null,
    val sessionState: MdocSessionState = MdocSessionState.Idle,
    val lastErrorMessage: String? = null,
    val bluetoothState: BluetoothState = BluetoothState.Unknown,
    val hasBluetoothPermissions: Boolean? = null
)
