package uk.gov.onelogin.sharing.holder.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.bluetooth.api.AdvertiserStartResult
import uk.gov.onelogin.sharing.bluetooth.api.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiser
import uk.gov.onelogin.sharing.holder.engagement.Engagement
import uk.gov.onelogin.sharing.holder.engagement.EngagementAlgorithms.EC_ALGORITHM
import uk.gov.onelogin.sharing.holder.engagement.EngagementAlgorithms.EC_PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity

class HolderWelcomeViewModel(
    private val sessionSecurity: SessionSecurity,
    private val engagementGenerator: Engagement,
    private val bleAdvertiser: BleAdvertiser,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val _uiState = MutableStateFlow(HolderWelcomeUiState())
    val uiState: StateFlow<HolderWelcomeUiState> = _uiState

    init {
        viewModelScope.launch(dispatcher) {
            val pubKey = sessionSecurity.generateEcPublicKey(EC_ALGORITHM, EC_PARAMETER_SPEC)
            val key = pubKey?.let { CoseKey.generateCoseKey(it) }
            if (key != null) {
                val engagement = engagementGenerator.qrCodeEngagement(key, _uiState.value.uuid)
                _uiState.update { it.copy(qrData = "mdoc:$engagement") }
            }
        }

        viewModelScope.launch {
            bleAdvertiser.state.collect { state ->
                _uiState.update { it.copy(advertiserState = state) }
            }
        }
    }

    fun onStartAdvertise() {
        val uuid = _uiState.value.uuid
        viewModelScope.launch {
            val result = bleAdvertiser.startAdvertise(BleAdvertiseData(serviceUuid = uuid))
            if (result is AdvertiserStartResult.Error) {
                _uiState.update { it.copy(lastErrorMessage = result.error) }
            }
        }
    }

    fun onStopAdvertise() {
        viewModelScope.launch {
            bleAdvertiser.stopAdvertise()
        }
    }

    fun onErrorMessageShown() {
        _uiState.update { it.copy(lastErrorMessage = null) }
    }
}

data class HolderWelcomeUiState(
    val uuid: UUID = UUID.randomUUID(),
    val qrData: String? = null,
    val advertiserState: AdvertiserState = AdvertiserState.Idle,
    val lastErrorMessage: String? = null
)
