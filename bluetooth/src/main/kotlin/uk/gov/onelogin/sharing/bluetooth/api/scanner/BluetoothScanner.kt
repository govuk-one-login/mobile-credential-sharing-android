package uk.gov.onelogin.sharing.bluetooth.api.scanner

import android.Manifest
import androidx.annotation.RequiresPermission

fun interface BluetoothScanner {
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    suspend fun scan(
        scanningPeriodMilliseconds: Long,
        callback: ScannerCallback,
        peripheralServerModeUuids: List<ByteArray>
    )

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    suspend fun scan(
        scanningPeriodMilliseconds: Long,
        callback: ScannerCallback,
        vararg peripheralServerModeUuids: ByteArray
    ) = scan(
        scanningPeriodMilliseconds = scanningPeriodMilliseconds,
        callback = callback,
        peripheralServerModeUuids = peripheralServerModeUuids.toList()
    )
}
