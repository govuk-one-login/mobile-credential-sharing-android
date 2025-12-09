package uk.gov.onelogin.sharing.bluetooth.api.scanner

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log

class ConnectDevicesToGattServer(
    private val callback: BluetoothGattCallback,
    private val context: Context,
    private val autoConnectToGattServer: Boolean = false,
    private val onConnectToGattServer: (BluetoothGatt) -> Unit = {}
) : ScannerCallback {
    override val onBatchResults: (List<ScanResult>) -> Unit
        @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
        get() = { results ->
            results.forEach {
                try {
                    it.device.connectGatt(
                        context,
                        autoConnectToGattServer,
                        callback
                    ).also(onConnectToGattServer::invoke)
                } catch (exception: IllegalArgumentException) {
                    Log.e(
                        this::class.java.simpleName,
                        "Couldn't connect to GATT server: ${it.device.address}",
                        exception
                    )
                }
            }
        }

    override val onFailure: (ScannerFailure) -> Unit
        get() = {
            Log.e(
                this::class.java.simpleName,
                "Bluetooth scanning failed: $it"
            )
        }

    override val onResult: (Int, ScanResult) -> Unit
        get() = { _, result ->
            onBatchResults(listOf(result))
        }
}
