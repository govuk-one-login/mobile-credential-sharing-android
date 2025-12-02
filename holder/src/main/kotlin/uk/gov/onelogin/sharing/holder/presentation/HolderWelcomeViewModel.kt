package uk.gov.onelogin.sharing.holder.presentation

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
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionError
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionManagerFactory
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionState
import uk.gov.onelogin.sharing.bluetooth.api.SessionManagerFactory
import uk.gov.onelogin.sharing.bluetooth.internal.core.BluetoothStatus
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
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    companion object {
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

                        return HolderWelcomeViewModel(
                            sessionSecurity = SessionSecurityImpl(),
                            engagementGenerator = EngagementGenerator(),
                            mdocSessionManagerFactory = mdocFactory
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
                        println("Mdoc - Advertising Started UUID: ${_uiState.value.uuid}")

                    MdocSessionState.AdvertisingStopped ->
                        println("Mdoc - Advertising Stopped")

                    is MdocSessionState.Connected ->
                        println("Mdoc - Connected: ${state.address}")

                    is MdocSessionState.Disconnected ->
                        println("Mdoc - Disconnected: ${state.address}")

                    is MdocSessionState.Error ->
                        handleError(state.reason)

                    MdocSessionState.GattServiceStopped ->
                        println("Mdoc - GattService Stopped")

                    MdocSessionState.Idle ->
                        println("Mdoc - Idle")

                    is MdocSessionState.ServiceAdded ->
                        println("Mdoc - Service Added: ${state.uuid}")
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
                            println("Mdoc - Bluetooth switched OFF")
                            _uiState.update {
                                it.copy(bluetoothState = BluetoothState.Disabled)
                            }
                        }
                    }

                    BluetoothStatus.TURNING_ON -> {
                        println("Mdoc - Bluetooth initializing")
                        _uiState.update {
                            it.copy(bluetoothState = BluetoothState.Initializing)
                        }
                    }

                    BluetoothStatus.ON -> {
                        println("Mdoc - Bluetooth switched ON")
                        val wasEnabled = _uiState.value.bluetoothState == BluetoothState.Enabled
                        _uiState.update { it.copy(bluetoothState = BluetoothState.Enabled) }

                        if (!wasEnabled && _uiState.value.hasBluetoothPermissions == true) {
                            mdocBleSession.start(_uiState.value.uuid)
                        }
                    }

                    BluetoothStatus.UNKNOWN ->
                        println("Mdoc - Bluetooth status unknown")
                }
            }
        }
    }

    private fun handleError(reason: MdocSessionError) {
        when (reason) {
            MdocSessionError.ADVERTISING_FAILED -> println("Mdoc - Error: Advertising failed")

            MdocSessionError.GATT_NOT_AVAILABLE -> println("Mdoc - Error: GATT not available")

            MdocSessionError.BLUETOOTH_PERMISSION_MISSING ->
                println("Mdoc - Error: Bluetooth permission missing")
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
    }
}

data class HolderWelcomeUiState(
    val uuid: UUID = UUID.randomUUID(),
    val qrData: String? = null,
    val sessionState: MdocSessionState = MdocSessionState.Idle,
    val lastErrorMessage: String? = null,
    val bluetoothState: BluetoothState = BluetoothState.Unknown,
    val hasBluetoothPermissions: Boolean? = null
)
