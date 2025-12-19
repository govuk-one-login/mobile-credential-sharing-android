package uk.gov.onelogin.sharing.bluetooth.api.scanner

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeAndroidBluetoothScanner(val flow: Flow<ScanEvent> = MutableSharedFlow()) :
    BluetoothScanner {
    var scanCalls = 0
    var lastUuid: ByteArray? = null

    override fun scan(serviceUuid: ByteArray): Flow<ScanEvent> {
        scanCalls++
        lastUuid = serviceUuid
        return flow
    }
}
