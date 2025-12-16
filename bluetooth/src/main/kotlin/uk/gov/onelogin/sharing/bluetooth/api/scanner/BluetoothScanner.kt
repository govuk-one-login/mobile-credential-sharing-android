package uk.gov.onelogin.sharing.bluetooth.api.scanner

import android.bluetooth.le.ScanResult
import kotlinx.coroutines.flow.Flow

interface BluetoothScanner {
    fun scan(peripheralServerModeUuid: ByteArray): Flow<ScanResult>
}
