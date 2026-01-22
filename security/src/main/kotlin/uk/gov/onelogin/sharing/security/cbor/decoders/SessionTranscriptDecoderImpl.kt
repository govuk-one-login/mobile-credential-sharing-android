package uk.gov.onelogin.sharing.security.cbor.decoders

import com.fasterxml.jackson.core.exc.StreamReadException
import com.fasterxml.jackson.databind.DatabindException
import java.io.IOException
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.security.cbor.base64Decode
import uk.gov.onelogin.sharing.security.cbor.decodeSessionEstablishmentModel
import uk.gov.onelogin.sharing.security.cbor.encodeCbor
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCbor

/**
 * Standard [SessionTranscriptDecoder] implementation, converting device engagement and
 * session establishment data into a session transcript.
 */
class SessionTranscriptDecoderImpl(private val logger: Logger) : SessionTranscriptDecoder {
    @Throws(
        IllegalArgumentException::class,
        IOException::class,
        StreamReadException::class,
        DatabindException::class
    )
    override fun deriveSessionTranscript(
        cborBase64Url: String,
        sessionEstablishmentBytes: ByteArray
    ): ByteArray = try {
        val result = deriveSessionTranscriptBytes(
            cborBase64Url = cborBase64Url,
            sessionEstablishmentBytes = sessionEstablishmentBytes
        )

        EmbeddedCbor(result).encodeCbor().also {
            logger.debug(
                logTag,
                "Successfully derived session transcript $LOG_MESSAGE_SUFFIX"
            )
        }
    } catch (exception: IllegalArgumentException) {
        logger.error(
            logTag,
            "Cannot derive session transcript $LOG_MESSAGE_SUFFIX",
            exception
        )

        throw exception
    }

    /**
     * Generates a [ByteArray] with the proceeding ordering:
     * - Base-64 decoded representation of [cborBase64Url].
     * - [EmbeddedCbor.encoded] value of
     *   [uk.gov.onelogin.sharing.security.cbor.dto.SessionEstablishmentDto.eReaderKey] generated
     *   from [sessionEstablishmentBytes].
     * - `null`, as there's no current need for a `Handover` value.
     *
     * @return A concatenated [ByteArray], containing the preceding list of elements.
     *
     * @see base64Decode
     * @see decodeSessionEstablishmentModel
     */
    @Throws(
        IllegalArgumentException::class,
        IOException::class,
        StreamReadException::class,
        DatabindException::class
    )
    private fun deriveSessionTranscriptBytes(
        cborBase64Url: String,
        sessionEstablishmentBytes: ByteArray
    ): ByteArray {
        var result = byteArrayOf()

        arrayOf(
            cborBase64Url.base64Decode(),
            decodeSessionEstablishmentModel(
                rawBytes = sessionEstablishmentBytes,
                logger = logger
            ).eReaderKey.encoded,
            null
        ).also {
            logger.debug(
                this.logTag,
                "Created session transcript array $LOG_MESSAGE_SUFFIX"
            )
        }.forEach { element ->
            element?.let {
                result = result + it
            }
        }
        return result
    }

    companion object {
        private const val LOG_MESSAGE_SUFFIX = "from encoded device engagement and eReader bytes"
    }
}
