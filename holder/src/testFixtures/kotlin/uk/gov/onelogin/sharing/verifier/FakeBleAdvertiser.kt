package uk.gov.onelogin.sharing.verifier

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.bluetooth.api.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.api.StartAdvertisingException

class FakeBleAdvertiser(initialState: AdvertiserState = AdvertiserState.Idle) : BleAdvertiser {
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<AdvertiserState> = _state

    var startCalls = 0
    var stopCalls = 0
    var lastAdvertiseData: BleAdvertiseData? = null
    var exceptionToThrow: StartAdvertisingException? = null

    override suspend fun startAdvertise(bleAdvertiseData: BleAdvertiseData) {
        if (exceptionToThrow != null) {
            throw exceptionToThrow!!
        }
        startCalls++
        lastAdvertiseData = bleAdvertiseData
        _state.value = AdvertiserState.Starting
        _state.value = AdvertiserState.Started
    }

    override suspend fun stopAdvertise() {
        stopCalls++
        _state.value = AdvertiserState.Stopped
    }

    override fun isBluetoothEnabled(): Boolean = true

    override fun hasAdvertisePermission(): Boolean = true

    fun emitState(state: AdvertiserState) {
        _state.value = state
    }
}
