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

    object StubData {
        /**
         * Byte array used within testing.
         *
         * As this byte array isn't a valid service UUID, developers are expected to utilise mocking
         * frameworks to map this input into required information, such as a
         * [android.bluetooth.BluetoothDevice].
         */
        val dummyByteArray = byteArrayOf(0x01, 0x02, 0x03)
    }
}
