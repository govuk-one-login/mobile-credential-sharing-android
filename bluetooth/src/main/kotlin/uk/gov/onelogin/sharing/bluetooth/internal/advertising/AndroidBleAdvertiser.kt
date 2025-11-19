package uk.gov.onelogin.sharing.bluetooth.internal.advertising

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import uk.gov.onelogin.sharing.bluetooth.api.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.api.AdvertisingError
import uk.gov.onelogin.sharing.bluetooth.api.AdvertisingFailureReason
import uk.gov.onelogin.sharing.bluetooth.api.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.api.BleUuidValidator
import uk.gov.onelogin.sharing.bluetooth.api.StartAdvertisingException
import uk.gov.onelogin.sharing.bluetooth.api.permissions.PermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.core.BleProvider

internal class AndroidBleAdvertiser(
    private val bleProvider: BleProvider,
    private val permissionChecker: PermissionChecker,
    private val startTimeoutMs: Long = 5_000
) : BleAdvertiser {

    private val _state = MutableStateFlow<AdvertiserState>(AdvertiserState.Idle)
    override val state: StateFlow<AdvertiserState> = _state

    private var currentCallback: AdvertisingCallback? = null

    override fun isBluetoothEnabled() = bleProvider.isBluetoothEnabled()
    override fun hasAdvertisePermission() = permissionChecker.hasPermission()

    override suspend fun startAdvertise(bleAdvertiseData: BleAdvertiseData) {
        when {
            !bleProvider.isBluetoothEnabled() ->
                throw StartAdvertisingException(
                    AdvertisingError.BLUETOOTH_DISABLED
                )

            !permissionChecker.hasPermission() ->
                throw StartAdvertisingException(
                    AdvertisingError.MISSING_PERMISSION
                )

            !BleUuidValidator.isValid(bleAdvertiseData.serviceUuid) ->
                throw StartAdvertisingException(
                    AdvertisingError.INVALID_UUID
                )

            _state.value == AdvertiserState.Starting ||
                _state.value == AdvertiserState.Started ->
                throw StartAdvertisingException(
                    AdvertisingError.ALREADY_IN_PROGRESS
                )

            else -> {
                _state.value = AdvertiserState.Starting

                try {
                    withTimeout(startTimeoutMs) {
                        start(
                            AdvertisingParameters(),
                            bleAdvertiseData
                        )
                    }
                } catch (e: TimeoutCancellationException) {
                    println(e.message)
                    throw StartAdvertisingException(
                        AdvertisingError.START_TIMEOUT
                    )
                } catch (e: CancellationException) {
                    println(e.message)
                    throw e
                } catch (e: IllegalStateException) {
                    println(e.message)
                    throw StartAdvertisingException(
                        AdvertisingError.INTERNAL_ERROR
                    )
                }
            }
        }
    }

    private suspend fun start(parameters: AdvertisingParameters, data: BleAdvertiseData) =
        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                doStopAdvertising()
            }

            currentCallback = StartAdvertisingCallback(
                onStateChange = { _state.value = it },
                continuation = continuation
            )

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
        doStopAdvertising()
    }

    private fun doStopAdvertising() {
        _state.value = AdvertiserState.Stopping
        runCatching { bleProvider.stopAdvertising() }
        currentCallback = null
        _state.value = AdvertiserState.Stopped
    }
}

private class StartAdvertisingCallback(
    private val onStateChange: (AdvertiserState) -> Unit,
    private val continuation: CancellableContinuation<Unit>
) : AdvertisingCallback {
    override fun onAdvertisingStarted() {
        onStateChange(AdvertiserState.Started)
        continuation.resume(Unit)
    }

    override fun onAdvertisingStartFailed(reason: AdvertisingFailureReason) {
        onStateChange(AdvertiserState.Failed("start failed: $reason"))
        if (continuation.isActive) {
            continuation.resume(Unit)
        }
    }

    override fun onAdvertisingStopped() {
        onStateChange(AdvertiserState.Stopped)
    }
}
