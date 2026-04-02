package uk.gov.onelogin.sharing.bluetooth.scanner

import java.util.UUID
import kotlinx.coroutines.flow.Flow
import uk.gov.onelogin.sharing.bluetooth.api.scanner.BluetoothScanner
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScanEvent

/**
 * [BluetoothScanner] implementation that throws an exception when interacted with.
 *
 * Use this when a test requires the existence of a [BluetoothScanner], yet doesn't interact with
 * it.
 */
object DummyBluetoothScanner : BluetoothScanner {
    override fun scan(serviceUuid: UUID): Flow<ScanEvent> =
        throw IllegalStateException("This is a dummy object and shouldn't be used!")
}
