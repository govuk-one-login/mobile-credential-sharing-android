package uk.gov.onelogin.sharing.bluetooth.api.scanner

import android.Manifest
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import java.util.concurrent.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import uk.gov.onelogin.sharing.bluetooth.api.adapter.BluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScannerCallback.Companion.toLeScanCallback
import uk.gov.onelogin.sharing.core.UUIDExtensions.toUUID

@ContributesBinding(ViewModelScope::class)
@Inject
class AndroidBluetoothScanner(bluetoothLeScanner: BluetoothAdapterProvider) : BluetoothScanner {

    private val scanner = bluetoothLeScanner.getLeScanner()
    private var isScanning = false

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun scan(peripheralServerModeUuid: ByteArray): Flow<ScanResult> = callbackFlow {
        val callbackLogic = ScannerCallback.of(
            onResult = { _, result ->
                Log.d(
                    AndroidBluetoothScanner::class.java.simpleName,
                    "Found device: ${result.device.address}"
                )
                trySend(result)
            },
            onFailure = { failure ->
                isScanning = false
                cancel(CancellationException("Scan failed: $failure"))
            }
        )

        val leScanCallback: ScanCallback = callbackLogic.toLeScanCallback()

        Log.d(
            AndroidBluetoothScanner::class.java.simpleName,
            "Creating ScanFilter for UUID: ${peripheralServerModeUuid.toUUID()}"
        )

        val filters: List<ScanFilter> = listOf(
            ScanFilter.Builder()
                .setServiceUuid(
                    ParcelUuid.fromString(
                        peripheralServerModeUuid.toUUID().toString()
                    )
                )
                .build()
        )

        isScanning = true

        scanner?.startScan(
            filters,
            ScanSettings.Builder()
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setLegacy(false)
                .build(),
            leScanCallback
        )

        awaitClose {
            Log.d(AndroidBluetoothScanner::class.simpleName, "Stopping scanner")
            isScanning = false
            scanner?.stopScan(leScanCallback)
        }
    }
}
