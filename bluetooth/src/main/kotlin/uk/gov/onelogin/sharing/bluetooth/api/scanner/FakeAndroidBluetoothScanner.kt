package uk.gov.onelogin.sharing.bluetooth.api.scanner

import android.bluetooth.le.ScanResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeAndroidBluetoothScanner(val flow: Flow<ScanResult> = MutableSharedFlow()) :
    BluetoothScanner {
    var scanCalls = 0
    var lastUuid: ByteArray? = null

    override fun scan(peripheralServerModeUuid: ByteArray): Flow<ScanResult> {
        scanCalls++
        lastUuid = peripheralServerModeUuid
        return flow
    }
}
