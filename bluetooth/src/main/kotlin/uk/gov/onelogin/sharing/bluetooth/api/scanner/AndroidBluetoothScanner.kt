package uk.gov.onelogin.sharing.bluetooth.api.scanner

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import java.util.UUID
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScannerCallback.Companion.toLeScanCallback
import uk.gov.onelogin.sharing.core.UUIDExtensions.toUUID

class AndroidBluetoothScanner(private val getBluetoothScanner: () -> BluetoothLeScanner) :
    BluetoothScanner {

    private var isScanning = false

    constructor(context: Context) : this(
        getBluetoothScanner = {
            (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
                .adapter
                .bluetoothLeScanner
        }
    )

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override suspend fun scan(
        scanningPeriodMilliseconds: Long,
        callback: ScannerCallback,
        peripheralServerModeUuids: List<ByteArray>
    ) {
        val leScanCallback = callback.toLeScanCallback()

        if (!isScanning) {
            performScan(
                scanningPeriodMilliseconds = scanningPeriodMilliseconds,
                peripheralServerModeUuids = peripheralServerModeUuids,
                leScanCallback = leScanCallback
            )
        } else {
            isScanning = false
            getBluetoothScanner().stopScan(leScanCallback)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private suspend fun performScan(
        scanningPeriodMilliseconds: Long,
        leScanCallback: ScanCallback,
        peripheralServerModeUuids: List<ByteArray>
    ) {
        val bluetoothLeScanner = getBluetoothScanner()

        val uuidsToScan = peripheralServerModeUuids
            .map { it.toUUID() }
            .map(UUID::toString).also {
                Log.d(
                    AndroidBluetoothScanner::class.java.simpleName,
                    "Scanning for UUIDs: $it"
                )
            }

        try {
            withTimeout(scanningPeriodMilliseconds) {
                isScanning = true
                bluetoothLeScanner.startScan(
                    uuidsToScan.map {
                        ParcelUuid.fromString(it)
                        ScanFilter
                            .Builder()
                            .setServiceUuid(ParcelUuid.fromString(it))
                            .build()
                    },
                    ScanSettings.Builder().build(),
                    leScanCallback
                )
            }
        } catch (exception: TimeoutCancellationException) {
            Log.w(
                AndroidBluetoothScanner::class.java.simpleName,
                "Timeout occurred when scanning for UUIDs.",
                exception
            )
        } finally {
            isScanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }
}
