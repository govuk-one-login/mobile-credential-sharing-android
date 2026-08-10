package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.core.SharingSessionScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothTransport
import uk.gov.onelogin.sharing.core.logger.logTag

@ContributesBinding(scope = SharingSessionScope::class, binding = binding<HolderSessionTerminator>())
class HolderSessionTerminatorImpl(
    private val peripheralBluetoothTransport: PeripheralBluetoothTransport,
    private val logger: Logger
) : HolderSessionTerminator {

    override suspend fun terminate(serviceUuid: UUID) {
        logger.debug(logTag, "Waiting ${TERMINATION_DELAY_MS}ms before sending GATT End")
        delay(TERMINATION_DELAY_MS.milliseconds)

        logger.debug(logTag, "Sending GATT End (0x02) to verifier")
        peripheralBluetoothTransport.notifySessionEnd(serviceUuid)

        logger.debug(logTag, "Stopping BLE peripheral")
        peripheralBluetoothTransport.stop(serviceUuid, sendEndCommand = false)

        logger.debug(logTag, "Holder session terminated")
    }

    companion object {
        const val TERMINATION_DELAY_MS = 500L
    }
}
