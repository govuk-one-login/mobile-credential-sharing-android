package uk.gov.onelogin.sharing.security.cbor.decoders

import com.fasterxml.jackson.core.exc.StreamReadException
import com.fasterxml.jackson.databind.DatabindException
import java.io.IOException
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.security.cbor.base64Decode
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
        eReaderKeyTagged: ByteArray
    ): ByteArray {
        require(
            eReaderKeyTagged.size >= 2 &&
                eReaderKeyTagged[0] == 0xD8.toByte() &&
                eReaderKeyTagged[1] == 0x18.toByte()
        ) {
            logger.error(
                logTag,
                "Cannot derive session transcript from encoded device engagement " +
                    "and eReader bytes"
            )
            "CBOR parsing error: eReaderKey must be tag(24)"
        }

        val deviceEngagementBytes = cborBase64Url.base64Decode()
        val taggedDevEng = EmbeddedCbor(deviceEngagementBytes).encodeCbor()

        val out = java.io.ByteArrayOutputStream()
        out.write(0x83) // array
        out.write(taggedDevEng) // element #1
        out.write(eReaderKeyTagged) // element #2
        out.write(0xF6) // element #3 null

        logger.debug(
            logTag,
            "Successfully derived session transcript from encoded device " +
                "engagement and eReader bytes"
        )

        return out.toByteArray()
    }
}
