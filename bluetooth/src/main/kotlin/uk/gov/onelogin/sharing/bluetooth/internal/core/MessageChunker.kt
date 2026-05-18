package uk.gov.onelogin.sharing.bluetooth.internal.core

import kotlinx.coroutines.delay
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallback.Companion.LAST_PART
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallback.Companion.NON_LAST_PART

private const val LOG_TAG = "MessageChunker"
private const val INTER_CHUNK_DELAY_MS = 20L

/**
 * Centralised chunking logic for BLE message transmission.
 * Splits [data] into MTU-sized chunks with ISO header bytes and writes each via [writeChunk].
 * Suspends between intermediate chunks to allow the BLE stack to flush each write.
 *
 * @param data The full payload to transmit.
 * @param mtu The negotiated MTU value.
 * @param logger Logger instance.
 * @param writeChunk Callback to write a single chunk; returns `true` on success.
 * @return `true` if all chunks were sent successfully, `false` otherwise.
 */
internal suspend fun sendChunkedMessage(
    data: ByteArray,
    mtu: Int,
    logger: Logger,
    writeChunk: (chunk: ByteArray) -> Boolean
): Boolean {
    if (data.isEmpty()) {
        logger.error(LOG_TAG, "sendChunkedMessage called with empty data")
        return false
    }

    val chunkSize = MtuValues.dataChunkSize(mtu)
    logger.debug(LOG_TAG, "Sending ${data.size} bytes in chunks of $chunkSize (MTU=$mtu)")

    var offset = 0
    while (offset < data.size) {
        val end = minOf(offset + chunkSize, data.size)
        val isLast = end == data.size
        val header = if (isLast) LAST_PART else NON_LAST_PART
        val chunk = byteArrayOf(header) + data.copyOfRange(offset, end)

        if (!writeChunk(chunk)) {
            logger.error(LOG_TAG, "Failed to write SessionEstablishment packet at offset $offset")
            return false
        }

        if (isLast) {
            logger.debug(LOG_TAG, "Final SessionEstablishment chunk generated and sent")
        } else {
            logger.debug(
                LOG_TAG,
                "Intermediate SessionEstablishment chunk generated, more data will follow"
            )
            delay(INTER_CHUNK_DELAY_MS)
        }
        offset = end
    }

    logger.debug(LOG_TAG, "SessionEstablishment transmission complete")
    return true
}
