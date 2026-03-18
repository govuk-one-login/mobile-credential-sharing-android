package uk.gov.onelogin.sharing.security.cbor.decoders

import com.fasterxml.jackson.core.exc.StreamReadException
import com.fasterxml.jackson.databind.DatabindException
import java.io.ByteArrayOutputStream
import java.io.IOException
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.security.cbor.base64Decode
import uk.gov.onelogin.sharing.security.cbor.encodeCbor
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCbor

private const val CBOR_ARRAY = 0x83
private const val CBOR_NULL = 0xF6

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
        taggedEReaderKey: ByteArray
    ): ByteArray {
        require(
            taggedEReaderKey.size >= 2 &&
                taggedEReaderKey[0] == 0xD8.toByte() &&
                taggedEReaderKey[1] == 0x18.toByte()
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

        val encodedSessionTranscript = createCborArray(
            taggedDeviceEngagement = taggedDevEng,
            eReaderKeyTagged = taggedEReaderKey
        )

        logger.debug(
            logTag,
            "Successfully derived session transcript from encoded device " +
                "engagement and eReader bytes"
        )

        return encodedSessionTranscript
    }

    /**
     * creates cbor array with the following structure:
     *
     *  [
     *      tag24(btsr(DeviceEngagementBytes)
     *      tag24(btsr(COSEKeyBytes)
     *      null
     *  ]
     */
    private fun createCborArray(
        taggedDeviceEngagement: ByteArray,
        eReaderKeyTagged: ByteArray
    ): ByteArray = ByteArrayOutputStream().use { out ->
        out.write(CBOR_ARRAY) // array
        out.write(taggedDeviceEngagement) // element #1
        out.write(eReaderKeyTagged) // element #2
        out.write(CBOR_NULL) // element #3 null
        out.toByteArray()
    }
}
