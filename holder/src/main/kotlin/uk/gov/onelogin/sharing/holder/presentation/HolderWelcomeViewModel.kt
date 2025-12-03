package uk.gov.onelogin.sharing.holder.presentation

import android.R.attr.tag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
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
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionManagerFactory
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionState
import uk.gov.onelogin.sharing.bluetooth.api.SessionManagerFactory
import uk.gov.onelogin.sharing.bluetooth.internal.core.BluetoothStatus
import uk.gov.onelogin.sharing.core.logger.AndroidLoggerFactory
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.engagement.Engagement
import uk.gov.onelogin.sharing.security.engagement.EngagementAlgorithms.EC_ALGORITHM
import uk.gov.onelogin.sharing.security.engagement.EngagementAlgorithms.EC_PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.engagement.EngagementGenerator
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurityImpl

class HolderWelcomeViewModel(
    private val sessionSecurity: SessionSecurity,
    private val engagementGenerator: Engagement,
    private val mdocSessionManagerFactory: SessionManagerFactory,
    private val logger: Logger,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    companion object {
        private const val TAG = "HolderWelcomeViewModel"

        // This can be removed when DI is added
        @Composable
        fun holderWelcomeViewModel(): HolderWelcomeViewModel {
            val appContext = LocalContext.current.applicationContext

            val factory = remember {
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        require(
                            modelClass.isAssignableFrom(
                                HolderWelcomeViewModel::class.java
                            )
                        ) {
                            "Unknown ViewModel class $modelClass"
                        }

                        val mdocFactory = MdocSessionManagerFactory(appContext)
                        val logger = AndroidLoggerFactory.create()

                        return HolderWelcomeViewModel(
                            sessionSecurity = SessionSecurityImpl(logger),
                            engagementGenerator = EngagementGenerator(logger),
                            mdocSessionManagerFactory = mdocFactory,
                            logger = logger
                        ) as T
                    }
                }
            }

            return viewModel(factory = factory)
        }
    }

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
                logger.debug(TAG, "Mdoc - BLE state: $state")
                _uiState.update { it.copy(sessionState = state) }
                if (state == MdocSessionState.AdvertisingStarted) {
                    logger.debug(TAG, "Mdoc - Advertising UUID: ${_uiState.value.uuid}")
                }

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
