package uk.gov.onelogin.sharing.bluetooth.api.scanner

import android.Manifest
import android.bluetooth.le.ScanResult
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.Flow

fun interface BluetoothScanner {
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun scan(
        scanningPeriodMilliseconds: Long,
        peripheralServerModeUuids: List<ByteArray>
    ): Flow<ScanResult>

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun scan(
        scanningPeriodMilliseconds: Long,
        callback: ScannerCallback,
        vararg peripheralServerModeUuids: ByteArray
    ) = scan(
        scanningPeriodMilliseconds = scanningPeriodMilliseconds,
        peripheralServerModeUuids = peripheralServerModeUuids.toList()
    )
}
