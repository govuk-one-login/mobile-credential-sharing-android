package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import dev.zacsweers.metro.Inject
import java.io.ByteArrayOutputStream

@Inject
class DeviceAuthenticationEncoder {

    fun encode(
        sessionTranscriptBytes: ByteArray,
        docType: String,
        deviceNameSpacesBytes: ByteArray
    ): ByteArray {
        val sessionTranscript = unwrapTag24(sessionTranscriptBytes)

        val innerArray = ByteArrayOutputStream().also { out ->
            out.write(CBOR_ARRAY_4)
            CBORFactory().createGenerator(out)
                .use { gen -> gen.writeString(DEVICE_AUTHENTICATION_LABEL) }
            out.write(sessionTranscript)
            CBORFactory().createGenerator(out).use { gen -> gen.writeString(docType) }
            out.write(deviceNameSpacesBytes)
        }.toByteArray()

        return ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeTag(TAG_24)
                gen.writeBinary(innerArray)
            }
        }.toByteArray()
    }

    /**
     * Unwraps a CBOR Tag 24 envelope if present, returning the inner bstr content.
     *
     * SessionTranscriptBytes = #6.24(bstr .cbor SessionTranscript)
     *
     * @return raw SessionTranscript array
     */
    private fun unwrapTag24(bytes: ByteArray): ByteArray {
        if (bytes.size < MIN_TAG_24_SIZE ||
            bytes[0] != TAG_24_MARKER ||
            bytes[1] != TAG_24_VALUE
        ) {
            return bytes
        }
        val parser = CBORFactory().createParser(bytes) as CBORParser
        return parser.use { p ->
            p.nextToken()
            p.binaryValue
        }
    }

    private companion object {
        const val CBOR_ARRAY_4 = 0x84
        const val TAG_24 = 24
        const val TAG_24_MARKER = 0xD8.toByte()
        const val TAG_24_VALUE = 0x18.toByte()
        const val MIN_TAG_24_SIZE = 3
        const val DEVICE_AUTHENTICATION_LABEL = "DeviceAuthentication"
    }
}
