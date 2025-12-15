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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import uk.gov.onelogin.sharing.bluetooth.api.adapter.BluetoothAdapterProvider
import uk.gov.onelogin.sharing.core.UUIDExtensions.toUUID
import java.util.concurrent.CancellationException


@ContributesBinding(ViewModelScope::class)
@Inject
class AndroidBluetoothScanner(
    bluetoothLeScanner: BluetoothAdapterProvider
) :
    BluetoothScanner {

    private val scanner = bluetoothLeScanner.getLeScanner()
    private var isScanning = false

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun scan(
        scanningPeriodMilliseconds: Long,
        peripheralServerModeUuids: List<ByteArray>
    ): Flow<ScanResult> = callbackFlow {

        val callback = object : ScanCallback() {
            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)
            }

            override fun onScanFailed(errorCode: Int) {
                cancel(CancellationException("Error: $errorCode"))
            }

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                Log.d(AndroidBluetoothScanner::class.simpleName, result.toString())
                trySend(result!!)
            }
        }

        val filters = peripheralServerModeUuids.map { uuidBytes ->
            Log.d(
                AndroidBluetoothScanner::class.java.simpleName,
                "Creating ScanFilter for UUID: ${uuidBytes.toUUID()}"
            )
            val parcelUuid = ParcelUuid.fromString(uuidBytes.toUUID().toString())

            ScanFilter.Builder()
                .setServiceUuid(parcelUuid)
                .build()
        }

        isScanning = true

        scanner?.startScan(
            null, //supply filters variable here if we want to find our advertised wallet
            ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build(),
            callback
        )

        awaitClose {
            Log.d(AndroidBluetoothScanner::class.simpleName, "Stopping scanner")
            isScanning = false
            scanner?.stopScan(callback)
        }
    }

//    fun scanFlow(
//        scanningPeriodMilliseconds: Long,
//        leCallback: ScannerCallback,
//        peripheralServerModeUuids: List<ByteArray>
//    ): Flow<ScanResult> = callbackFlow {
//
//        val callback = object : ScanCallback() {
//            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
//                super.onBatchScanResults(results)
//            }
//
//            override fun onScanFailed(errorCode: Int) {
//                cancel(CancellationException("Error: $errorCode"))
//            }
//
//            override fun onScanResult(callbackType: Int, result: ScanResult?) {
//                Log.d(AndroidBluetoothScanner::class.simpleName, result.toString())
//                trySend(result!!)
//            }
//        }
//
//        val filters = peripheralServerModeUuids.map { uuidBytes ->
//            Log.d(
//                AndroidBluetoothScanner::class.java.simpleName,
//                "Creating ScanFilter for UUID: ${uuidBytes.toUUID()}"
//            )
//            val parcelUuid = ParcelUuid.fromString(uuidBytes.toUUID().toString())
//
//            ScanFilter.Builder()
//                .setServiceUuid(parcelUuid)
//                .build()
//        }
//
//        isScanning = true
//
//        scanner?.startScan(
//            filters,
//            ScanSettings.Builder()
//                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                .build(),
//            callback
//        )
//
//        awaitClose {
//            isScanning = false
//            scanner?.stopScan(callback)
//        }
//    }

//    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
//    private suspend fun performScan(
//        scanningPeriodMilliseconds: Long,
//        leScanCallback: ScanCallback,
//        peripheralServerModeUuids: List<ByteArray>
//    ) {
//        val filters = peripheralServerModeUuids.map { uuidBytes ->
//            Log.d(
//                AndroidBluetoothScanner::class.java.simpleName,
//                "Creating ScanFilter for UUID: ${uuidBytes.toUUID()}"
//            )
//            val parcelUuid = ParcelUuid.fromString(uuidBytes.toUUID().toString())
//
//            ScanFilter.Builder()
//                .setServiceUuid(parcelUuid)
//                .build()
//        }
//
//        try {
//            withTimeout(scanningPeriodMilliseconds) {
//                suspendCancellableCoroutine { continuation ->
//
//                    val callback = object : ScanCallback() {
//                        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
//                            super.onBatchScanResults(results)
//                        }
//
//                        override fun onScanFailed(errorCode: Int) {
//                            super.onScanFailed(errorCode)
//                        }
//
//                        override fun onScanResult(callbackType: Int, result: ScanResult?) {
//                            leScanCallback.onScanResult(callbackType, result!!)
//                            if (continuation.isActive) {
//                                continuation.resume(value = 1)
//                            }
//                            super.onScanResult(callbackType, result)
//
//                        }
//                    }
//
//                    isScanning = true
//                    scanner?.startScan(
//                        filters,
//                        ScanSettings.Builder()
//                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                            .build(),
//                        leScanCallback
//                    )
//
//                    continuation.invokeOnCancellation {
//                        isScanning = false
//                        scanner?.stopScan(leScanCallback)
//                        Log.d(AndroidBluetoothScanner::class.java.simpleName, "Scanning ended")
//                    }
//                }
//            }
//        } catch (exception: TimeoutCancellationException) {
//            Log.w(
//                AndroidBluetoothScanner::class.java.simpleName,
//                "Timeout occurred when scanning for UUIDs.",
//                exception
//            )
//        }
//    }
}
