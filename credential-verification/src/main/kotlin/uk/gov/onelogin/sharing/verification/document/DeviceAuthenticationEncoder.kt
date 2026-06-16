package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import dev.zacsweers.metro.Inject
import java.io.ByteArrayOutputStream

@Inject
class DeviceAuthenticationEncoder {

    fun encode(
        sessionTranscriptBytes: ByteArray,
        docType: String,
        deviceNameSpacesBytes: ByteArray
    ): ByteArray {
        val innerArray = ByteArrayOutputStream().also { out ->
            out.write(CBOR_ARRAY_4)
            CBORFactory().createGenerator(out)
                .use { gen -> gen.writeString(DEVICE_AUTHENTICATION_LABEL) }
            out.write(sessionTranscriptBytes)
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

    private companion object {
        const val CBOR_ARRAY_4 = 0x84
        const val TAG_24 = 24
        const val DEVICE_AUTHENTICATION_LABEL = "DeviceAuthentication"
    }
}
