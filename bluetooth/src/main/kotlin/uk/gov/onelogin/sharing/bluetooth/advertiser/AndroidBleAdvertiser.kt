package uk.gov.onelogin.sharing.bluetooth.advertiser

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingCallback
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingFailureReason
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.ble.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.ble.BleProvider
import uk.gov.onelogin.sharing.bluetooth.permissions.PermissionChecker

class AndroidBleAdvertiser(
    private val bleProvider: BleProvider,
    private val permissionChecker: PermissionChecker,
    private val startTimeoutMs: Long = 5_000
) : BleAdvertiser {

    private val _state = MutableStateFlow<AdvertiserState>(AdvertiserState.Idle)
    override val state: StateFlow<AdvertiserState> = _state

    private var currentCallback: AdvertisingCallback? = null

    override fun isBluetoothEnabled() = bleProvider.isBluetoothEnabled()
    override fun hasAdvertisePermission() = permissionChecker.hasPermission()

    override suspend fun startAdvertise(bleAdvertiseData: BleAdvertiseData): AdvertiserStartResult =
        when {
            !bleProvider.isBluetoothEnabled() -> {
                AdvertiserStartResult.Error("Bluetooth is disabled")
            }

            !permissionChecker.hasPermission() -> {
                AdvertiserStartResult.Error("Missing permissions")
            }

            _state.value == AdvertiserState.Starting ||
                _state.value == AdvertiserState.Started -> {
                AdvertiserStartResult.Error("Already starting")
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
                    println(e.message)
                    AdvertiserStartResult.Error("Advertising start timed out")
                } catch (e: IllegalStateException) {
                    println(e.message)
                    AdvertiserStartResult.Error("Failed to start advertising")
                }
            }
        }

    private suspend fun start(parameters: AdvertisingParameters, data: BleAdvertiseData) =
        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                _state.value = AdvertiserState.Stopping
                bleProvider.stopAdvertising()
                currentCallback = null
            }

            currentCallback = object : AdvertisingCallback {
                override fun onAdvertisingStarted() {
                    _state.value = AdvertiserState.Started
                    continuation.resume(Unit)
                }

                override fun onAdvertisingStartFailed(reason: AdvertisingFailureReason) {
                    _state.value = AdvertiserState.Failed("start failed: $reason")
                    continuation.resumeWithException(
                        IllegalStateException("start failed: $reason")
                    )
                }

                override fun onAdvertisingStopped() {
                    _state.value = AdvertiserState.Stopped
                }
            }

            try {
                currentCallback?.let {
                    bleProvider.startAdvertising(
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
        }

    override suspend fun stopAdvertise() {
        _state.value = AdvertiserState.Stopping
        runCatching { bleProvider.stopAdvertising() }
        currentCallback = null
        _state.value = AdvertiserState.Stopped
    }
}
