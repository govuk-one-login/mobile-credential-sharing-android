package uk.gov.onelogin.sharing.holder.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionState
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.engagement.Engagement
import uk.gov.onelogin.sharing.security.engagement.EngagementAlgorithms.EC_ALGORITHM
import uk.gov.onelogin.sharing.security.engagement.EngagementAlgorithms.EC_PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity
import java.util.UUID

@Inject
class HolderWelcomeViewModel(
    private val sessionSecurity: SessionSecurity,
    private val engagementGenerator: Engagement,
    private val mdocBleSession: MdocSessionManager,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val initialState = HolderWelcomeUiState()
    private val _uiState = MutableStateFlow(initialState)

    @OptIn(ExperimentalCoroutinesApi::class)
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
}

data class HolderWelcomeUiState(
    val uuid: UUID = UUID.randomUUID(),
    val qrData: String? = null,
    val sessionState: MdocSessionState = MdocSessionState.Idle,
    val lastErrorMessage: String? = null
)
