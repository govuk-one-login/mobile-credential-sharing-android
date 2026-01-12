package uk.gov.onelogin.sharing.bluetooth.api.scanner

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

fun interface BluetoothScanner {
    fun scan(serviceUuid: ByteArray): Flow<ScanEvent>

    companion object {
        /**
         * Factory function that wraps the provided [events] as a [BluetoothScanner] implementation.
         */
        fun from(events: Flow<ScanEvent>) = BluetoothScanner { events }

        /**
         * Factory function that wraps the provided [events] as a [BluetoothScanner] implementation.
         *
         * Internally converts the `vararg` into a [Flow].
         */
        fun of(vararg events: ScanEvent) = from(events.asFlow())

        /**
         * Factory function that wraps the provided [events] as a [BluetoothScanner] implementation.
         *
         * Internally converts the [Iterable] into a [Flow].
         */
        fun of(events: Iterable<ScanEvent>) = from(events.asFlow())
    }
}
