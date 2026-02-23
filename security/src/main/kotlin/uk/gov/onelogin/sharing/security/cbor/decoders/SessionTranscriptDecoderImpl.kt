package uk.gov.onelogin.sharing.security.cbor.decoders

import com.fasterxml.jackson.core.exc.StreamReadException
import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import java.io.IOException
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.security.cbor.base64Decode
import uk.gov.onelogin.sharing.security.cbor.decodeSessionEstablishmentModel
import uk.gov.onelogin.sharing.security.cbor.deriveUntaggedCbor
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
       deriveSessionTranscriptBytes(
            cborBase64Url = cborBase64Url,
            sessionEstablishmentBytes = sessionEstablishmentBytes
        )

       /* EmbeddedCbor(result).encodeCbor().also {
            logger.debug(
                logTag,
                "Successfully derived session transcript $LOG_MESSAGE_SUFFIX"
            )
        }*/
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
        val mapper = CBORMapper()

        val deviceEngagementBytes = cborBase64Url.base64Decode()

        val eReaderKeyTagged = decodeSessionEstablishmentModel(sessionEstablishmentBytes, logger).eReaderKey.encoded
        val eReaderKeyCoseKeyBytes = deriveUntaggedCbor(eReaderKeyTagged)

        val devEngNode = mapper.readTree(deviceEngagementBytes)
        val eReaderKeyNode = mapper.readTree(eReaderKeyCoseKeyBytes)

        val arr = mapper.createArrayNode()
        arr.add(devEngNode)
        arr.add(eReaderKeyNode)
        arr.addNull() // Handover = null

        val transcript = mapper.writeValueAsBytes(arr)

        logger.debug(logTag, "transcript[0]=0x${"%02x".format(transcript[0])} len=${transcript.size}")
        return transcript
    }

    companion object {
        private const val LOG_MESSAGE_SUFFIX = "from encoded device engagement and eReader bytes"
    }
}
