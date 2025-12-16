package uk.gov.onelogin.sharing.bluetooth.api.scanner

import android.bluetooth.le.ScanResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeAndroidBluetoothScanner : BluetoothScanner {
    private val flow = MutableSharedFlow<ScanResult>()

    override fun scan(peripheralServerModeUuid: ByteArray): Flow<ScanResult> = flow

    suspend fun emitScanResult(result: ScanResult) {
        flow.emit(result)
    }
}
