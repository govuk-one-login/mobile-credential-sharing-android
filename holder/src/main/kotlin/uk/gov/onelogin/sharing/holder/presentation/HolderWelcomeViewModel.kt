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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionFactory
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionState
import uk.gov.onelogin.sharing.holder.engagement.Engagement
import uk.gov.onelogin.sharing.holder.engagement.EngagementAlgorithms.EC_ALGORITHM
import uk.gov.onelogin.sharing.holder.engagement.EngagementAlgorithms.EC_PARAMETER_SPEC
import uk.gov.onelogin.sharing.holder.engagement.EngagementGenerator
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurityImpl

class HolderWelcomeViewModel(
    private val sessionSecurity: SessionSecurity,
    private val engagementGenerator: Engagement,
    private val mdocBleSession: MdocSessionManager,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    companion object {
        // This can be removed when DI is added
        @Composable
        fun holderWelcomeViewModel(): HolderWelcomeViewModel {
            val context = LocalContext.current

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

                        val mdocBleSession = MdocSessionFactory.create(context)

                        return HolderWelcomeViewModel(
                            sessionSecurity = SessionSecurityImpl(),
                            engagementGenerator = EngagementGenerator(),
                            mdocBleSession = mdocBleSession
                        ) as T
                    }
                }
            }

            return viewModel(factory = factory)
        }
    }

    private val initialState = HolderWelcomeUiState()
    private val _uiState = MutableStateFlow(initialState)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HolderWelcomeUiState> = _uiState

    init {
        checkBluetoothStatus()
        viewModelScope.launch(dispatcher) {
            val pubKey = sessionSecurity.generateEcPublicKey(EC_ALGORITHM, EC_PARAMETER_SPEC)
            val key = pubKey?.let { CoseKey.generateCoseKey(it) }
            if (key != null) {
                val engagement = engagementGenerator.qrCodeEngagement(key, _uiState.value.uuid)
                _uiState.update { it.copy(qrData = "mdoc:$engagement") }
            }
        }

        viewModelScope.launch {
            mdocBleSession.state.collect { state ->
                println("Advertiser state: $state")
                _uiState.update { it.copy(sessionState = state) }
                if (state == MdocSessionState.Started) {
                    println("Advertising UUID: ${_uiState.value.uuid}")
                }
            }
        }
    }

    fun startAdvertising() {
        val uuid = _uiState.value.uuid
        viewModelScope.launch {
            mdocBleSession.start(uuid)
        }
    }

    fun stopAdvertising() {
        viewModelScope.launch {
            mdocBleSession.stop()
        }
    }

    fun updateBluetoothState(state: BluetoothState) {
        _uiState.update {
            it.copy(bluetoothStatus = state)
        }
    }

    fun updateBluetoothPermissions(state: Boolean) {
        _uiState.update {
            it.copy(hasBluetoothPermissions = state)
        }
    }

    fun checkBluetoothStatus() {
        if (mdocBleSession.isBluetoothEnabled()) {
            _uiState.update {
                it.copy(bluetoothStatus = BluetoothState.Enabled)
            }
        } else {
            _uiState.update {
                it.copy(bluetoothStatus = BluetoothState.Disabled)
            }
        }
    }
}

data class HolderWelcomeUiState(
    val uuid: UUID = UUID.randomUUID(),
    val qrData: String? = null,
    val sessionState: MdocSessionState = MdocSessionState.Idle,
    val lastErrorMessage: String? = null,
    val bluetoothStatus: BluetoothState = BluetoothState.Initializing,
    val hasBluetoothPermissions: Boolean? = null
)
