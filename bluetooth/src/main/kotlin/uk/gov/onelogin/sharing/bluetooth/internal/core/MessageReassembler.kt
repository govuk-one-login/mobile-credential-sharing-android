package uk.gov.onelogin.sharing.bluetooth.internal.core

import java.util.UUID
import uk.gov.logging.api.v3.Logger

private const val LOG_TAG = "MessageReassembler"

/**
 * Centralised reassembly logic for chunked BLE messages. The receive-side counterpart to
 * [sendChunkedMessage].
 *
 * Incoming chunks carry a single ISO header byte ([NON_LAST_PART] / [LAST_PART]) followed by the
 * payload. This class strips the header, accumulates payload bytes per characteristic [UUID] and
 * reports the outcome of each chunk via [Result]. It performs no side effects beyond logging and
 * maintaining its internal buffer: callers decide how to react (emit events, disconnect, etc.).
 *
 * Instances are stateful (they hold per-characteristic buffers) and the [maxReceiveBufferSize]
 * guard differs per role, so instances are created via [Factory] rather than injected directly.
 *
 * @param messages Backing buffer map, keyed by characteristic UUID. Exposed for testing.
 */
class MessageReassembler(
    private val maxReceiveBufferSize: Int,
    private val logger: Logger,
    private val messages: MutableMap<UUID, ByteArray> = mutableMapOf()
) {
    sealed interface Result {
        data object Incomplete : Result

        data class Complete(val message: ByteArray) : Result

        /** The accumulated buffer exceeded [maxReceiveBufferSize]; the buffer has been discarded. */
        data class ExceededMaxBuffer(val bufferSize: Int) : Result

        data object InvalidPrefix : Result
    }

    /**
     * Accepts a single raw chunk (header byte + payload) for the given [uuid].
     *
     * On overflow the buffer for [uuid] is discarded. On completion the buffer for [uuid] is
     * cleared ready for the next message. Callers are responsible for any further side effects.
     */
    fun accept(uuid: UUID, value: ByteArray): Result {
        val previousMessages = messages[uuid] ?: byteArrayOf()
        val payload = value.drop(1).toByteArray()

        return when (val prefix = value.firstOrNull()) {
            NON_LAST_PART -> {
                val accumulated = previousMessages + payload
                if (accumulated.size > maxReceiveBufferSize) {
                    messages.remove(uuid)
                    logOverflow(accumulated.size)
                    Result.ExceededMaxBuffer(accumulated.size)
                } else {
                    messages[uuid] = accumulated
                    logger.debug(LOG_TAG, "received message part, expecting additional data")
                    Result.Incomplete
                }
            }

            LAST_PART -> {
                messages.remove(uuid)
                val fullMessage = previousMessages + payload

                if (fullMessage.size > maxReceiveBufferSize) {
                    logOverflow(fullMessage.size)
                    Result.ExceededMaxBuffer(fullMessage.size)
                } else {
                    logger.debug(LOG_TAG, "received complete message of ${fullMessage.size} bytes")
                    Result.Complete(fullMessage)
                }
            }

            else -> {
                val error = IllegalArgumentException(
                    "invalid message prefix received from remote device: ${prefix?.toHexString()}"
                )
                logger.error(
                    LOG_TAG,
                    "invalid message prefix received from remote device",
                    error
                )
                Result.InvalidPrefix
            }
        }
    }

    fun reset(uuid: UUID) {
        messages[uuid] = byteArrayOf()
    }

    /** Discards all accumulated buffers. Call when starting a fresh connection. */
    fun clear() {
        messages.clear()
    }

    private fun logOverflow(bufferSize: Int) {
        val message =
            "ExceededMaxBuffer: $bufferSize bytes exceeds limit of $maxReceiveBufferSize bytes"
        val thrown = IllegalStateException(message)
        logger.error(
            LOG_TAG,
            message,
            thrown
        )
    }

    /**
     * Creates [MessageReassembler] instances. Injectable as an [AppScope] singleton; each caller
     * supplies its own role-specific [maxReceiveBufferSize] when creating an instance.
     */
    fun interface Factory {
        fun create(maxReceiveBufferSize: Int): MessageReassembler
    }
}
