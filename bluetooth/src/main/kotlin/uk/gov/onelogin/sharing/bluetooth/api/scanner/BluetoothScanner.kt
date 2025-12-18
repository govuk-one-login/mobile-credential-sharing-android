package uk.gov.onelogin.sharing.bluetooth.api.scanner

import kotlinx.coroutines.flow.Flow

fun interface BluetoothScanner {
    fun scan(serviceUuid: ByteArray): Flow<ScanEvent>
}
