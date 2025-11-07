package uk.gov.onelogin.sharing.bluetooth.advertiser

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingCallback
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.ble.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.ble.BleProvider

class AndroidBleAdvertiser(
    private val bleProvider: BleProvider,
    private val startTimeoutMs: Long = 5_000
) : BleAdvertiser {

    private val _state = MutableStateFlow<AdvertiserState>(AdvertiserState.Idle)
    override val state: StateFlow<AdvertiserState> = _state

    private var currentCallback: AdvertisingCallback? = null

    override fun isBluetoothEnabled() = bleProvider.isBluetoothEnabled()
    override fun hasAdvertisePermission() = bleProvider.hasAdvertisePermission()

    override suspend fun startAdvertise(bleAdvertiseData: BleAdvertiseData): AdvertiserStartResult =
        when {
            !bleProvider.isBluetoothEnabled() -> {
                AdvertiserStartResult.Error("Bluetooth is disabled")
            }

            !bleProvider.hasAdvertisePermission() -> {
                AdvertiserStartResult.Error("Missing permissions")
            }

            else -> {
                _state.value = AdvertiserState.Starting

                try {
                    withTimeout(startTimeoutMs) {
                        start(
                            AdvertisingParameters(),
                            bleAdvertiseData
                        )
                    }
                    AdvertiserStartResult.Success
                } catch (e: TimeoutCancellationException) {
                    println("start failed")
                    AdvertiserStartResult.Error(e.message ?: "Advertising start timed out")
                } catch (e: IllegalStateException) {
                    AdvertiserStartResult.Error(e.message ?: "Failed to start advertising")
                }
            }
        }

    private suspend fun start(parameters: AdvertisingParameters, data: BleAdvertiseData) =
        suspendCancellableCoroutine { continuation ->
            currentCallback = object : AdvertisingCallback {
                override fun onAdvertisingStarted() {
                    _state.value = AdvertiserState.Started
                    continuation.resume(Unit)
                }

                override fun onAdvertisingFailed(status: Int) {
                    _state.value = AdvertiserState.Failed("start failed: status=$status")
                }

                override fun onAdvertisingStopped() {
                    _state.value = AdvertiserState.Stopped
                }
            }

            try {
                currentCallback?.let {
                    bleProvider.startAdvertisingSet(
                        parameters,
                        data,
                        it
                    )
                }
            } catch (e: IllegalStateException) {
                currentCallback = null
                _state.value = AdvertiserState.Failed(
                    e.message ?: "exception during start advertising"
                )
                if (continuation.isActive) {
                    continuation.resumeWithException(e)
                }
                return@suspendCancellableCoroutine
            }

            continuation.invokeOnCancellation {
                bleProvider.stopAdvertisingSet(currentCallback)
                currentCallback = null
            }
        }

    override suspend fun stopAdvertise() {
        _state.value = AdvertiserState.Stopping
        runCatching { bleProvider.stopAdvertisingSet(currentCallback) }
        currentCallback = null
        _state.value = AdvertiserState.Stopped
    }
}
