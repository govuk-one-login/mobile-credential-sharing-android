package uk.gov.onelogin.sharing.bluetooth.internal.central

import java.util.UUID
import kotlinx.coroutines.channels.Channel

/**
 * Coordinates BLE characteristic writes by suspending between each write until
 * the BLE stack signals readiness via [onWriteComplete].
 *
 * The BLE stack can only handle one write at a time. [awaitWriteConfirmation] suspends the
 * sender after each write, and [onWriteComplete] unblocks it once
 * [onCharacteristicWrite] fires for the [targetUuid].
 */
internal class GattWriteQueue(private val targetUuid: UUID) {
    private val channel = Channel<Boolean>(capacity = 1)

    /**
     * Suspends until [onWriteComplete] signals the write was accepted by the BLE stack.
     * @return `true` if the write succeeded, `false` if it failed.
     */
    suspend fun awaitWriteConfirmation(): Boolean = channel.receive()

    /**
     * Called from [onCharacteristicWrite]. Only signals if the characteristic
     * matches [targetUuid] to avoid stale confirmations from unrelated writes.
     */
    fun onWriteComplete(characteristicUuid: UUID, success: Boolean) {
        if (characteristicUuid == targetUuid) channel.trySend(success)
    }
}
