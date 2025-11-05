package uk.gov.onelogin.sharing.bluetooth.advertiser

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertisingSet
import android.bluetooth.le.AdvertisingSetCallback
import android.bluetooth.le.AdvertisingSetParameters
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class BleAdvertiserImpl(
    val context: Context
) : BleAdvertiser {
    private val _state = MutableStateFlow<AdvertiserState>(AdvertiserState.Idle)
    override val state: StateFlow<AdvertiserState> = _state

    override suspend fun startAdvertise(
        payload: BleAdvertiser.Payload,
        parameters: AdvertisingSetParameters
    ): AdvertiserStartResult {
        if (!isBluetoothEnabled()) {
            return AdvertiserStartResult.Error("Bluetooth is disabled")
        }

        if (!hasAdvertisePermission()) {
            return AdvertiserStartResult.Error("Missing permissions")
        }

        val advertisingParametersOverride = AdvertisingSetParameters.Builder()
            .setLegacyMode(false)
            .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
            .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MEDIUM)
            .setPrimaryPhy(BluetoothDevice.PHY_LE_1M)
            .setSecondaryPhy(BluetoothDevice.PHY_LE_2M)
            .setConnectable(true)
            .build()

        _state.value = AdvertiserState.Starting

        startAdvertising(
            payload,
            advertisingParametersOverride
        )

        return AdvertiserStartResult.Success
    }

    private suspend fun startAdvertising(
        payload: BleAdvertiser.Payload,
        parameters: AdvertisingSetParameters
    ) {
        try {
            suspendCancellableCoroutine { continuation ->
                val bluetoothManager = context.getSystemService(
                    Context.BLUETOOTH_SERVICE
                ) as BluetoothManager

                val bluetoothAdapter = bluetoothManager.adapter
                val bleAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

                val data = AdvertiseData.Builder()
                    .addServiceData(
                        ParcelUuid(UUID.randomUUID()),
                        payload.asBytes()
                    )
                    .build()


                val callback = object : AdvertisingSetCallback() {
                    override fun onAdvertisingSetStarted(
                        advertisingSet: AdvertisingSet?,
                        txPower: Int,
                        status: Int
                    ) {
                        if (status == ADVERTISE_SUCCESS) {
                            continuation.resume(advertisingSet)
                            _state.value = AdvertiserState.Started
                            println("Advertising started txpower: $txPower - status: $status")

                        } else {
                            _state.value = AdvertiserState.Failed(
                                "Advertising failed - status = $status"
                            )
                            println("Advertising failed - status = $status")
                            continuation.resumeWithException(
                                Exception(
                                    "Advertising failed"
                                )
                            )
                        }
                    }

                    override fun onAdvertisingSetStopped(advertisingSet: AdvertisingSet?) {
                        super.onAdvertisingSetStopped(advertisingSet)
                    }

                }

                try {
                    bleAdvertiser.startAdvertisingSet(
                        parameters,
                        data,
                        null,
                        null,
                        null,
                        callback
                    )
                } catch (e: Exception) {
                    println(e.message)
                }
            }
        } finally {

        }
    }

    override suspend fun stopAdvertise() {
    }

    override fun isBluetoothEnabled(): Boolean {
        return true
    }

    override fun hasAdvertisePermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ) == PackageManager.PERMISSION_GRANTED
    }
}