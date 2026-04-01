package uk.gov.onelogin.sharing.bluetooth.api.scanner

import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeAndroidBluetoothScanner(val flow: Flow<ScanEvent> = MutableSharedFlow()) :
    BluetoothScanner {
    var scanCalls = 0
    var lastUuid: UUID? = null

    override fun scan(serviceUuid: UUID): Flow<ScanEvent> {
        scanCalls++
        lastUuid = serviceUuid
        return flow
    }
}
