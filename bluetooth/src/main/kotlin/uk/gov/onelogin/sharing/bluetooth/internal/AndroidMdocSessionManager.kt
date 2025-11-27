package uk.gov.onelogin.sharing.bluetooth.internal

import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.bluetooth.api.MdocError
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionState
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.BleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.StartAdvertisingException
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.toMdocState

internal class AndroidMdocSessionManager(
    private val bleAdvertiser: BleAdvertiser,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : MdocSessionManager {
    private val _state = MutableStateFlow<MdocSessionState>(MdocSessionState.Idle)
    override val state: StateFlow<MdocSessionState> = _state

    init {
        coroutineScope.launch {
            bleAdvertiser.state.collect { advState ->
                _state.value = advState.toMdocState()
            }
        }
    }

    override suspend fun start(serviceUuid: UUID) {
        try {
            bleAdvertiser.startAdvertise(BleAdvertiseData(serviceUuid))
        } catch (e: StartAdvertisingException) {
            println("Error starting advertising: ${e.error}")
            _state.value = MdocSessionState.Error(MdocError.ADVERTISING_FAILED)
        }
    }

    override suspend fun stop() {
        bleAdvertiser.stopAdvertise()
        _state.value = MdocSessionState.Stopped
    }

    override fun isBluetoothEnabled(): Boolean = bleAdvertiser.isBluetoothEnabled()
}
