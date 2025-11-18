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
import uk.gov.onelogin.sharing.bluetooth.api.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiserFactory
import uk.gov.onelogin.sharing.bluetooth.api.StartAdvertisingException
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
    private val bleAdvertiser: BleAdvertiser,
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

                        val bleAdvertiser = BleAdvertiserFactory.create(context)

                        return HolderWelcomeViewModel(
                            sessionSecurity = SessionSecurityImpl(),
                            engagementGenerator = EngagementGenerator(),
                            bleAdvertiser = bleAdvertiser
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
                println("Advertiser state: $state")
                _uiState.update { it.copy(advertiserState = state) }
            }
        }
    }

    fun startAdvertising() {
        val uuid = _uiState.value.uuid
        viewModelScope.launch {
            try {
                bleAdvertiser.startAdvertise(BleAdvertiseData(serviceUuid = uuid))
            } catch (e: StartAdvertisingException) {
                println("Error starting advertising: ${e.error}")
                _uiState.update { it.copy(lastErrorMessage = e.message) }
            }
        }
    }

    fun stopAdvertising() {
        viewModelScope.launch {
            bleAdvertiser.stopAdvertise()
        }
    }
}

data class HolderWelcomeUiState(
    val uuid: UUID = UUID.randomUUID(),
    val qrData: String? = null,
    val advertiserState: AdvertiserState = AdvertiserState.Idle,
    val lastErrorMessage: String? = null
)
