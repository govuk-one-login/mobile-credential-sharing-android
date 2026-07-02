package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import uk.gov.onelogin.sharing.verification.document.CoseSign1Stubs.emptyDeviceNameSpacesBytes
import uk.gov.onelogin.sharing.verification.document.CoseSign1Stubs.sessionTranscriptBytes

class DeviceAuthenticationEncoderTest {
    private val cborMapper = ObjectMapper(CBORFactory())
    private val encoder = DeviceAuthenticationEncoder()

    @Test
    fun `encodes DeviceAuthenticationBytes as Tag 24 wrapping correct CBOR structure`() {
        val docType = "org.iso.18013.5.1.mDL"

        val result = encoder.encode(sessionTranscriptBytes, docType, emptyDeviceNameSpacesBytes)

        val outerNode = cborMapper.readTree(result)
        assertThat(outerNode.isBinary, equalTo(true))

        val innerBytes = (outerNode as BinaryNode).binaryValue()
        val inner = cborMapper.readTree(innerBytes) as ArrayNode

        assertThat(inner.size(), equalTo(4))
        assertThat(inner[0].asText(), equalTo("DeviceAuthentication"))
        assertThat(inner[1].isArray, equalTo(true))
        assertThat(inner[1].size(), equalTo(3))
        assertThat(inner[2].asText(), equalTo(docType))
        assertThat(inner[3].isBinary, equalTo(true))
    }

    /**
     * ISO 18013-5 §12.4.4: DeviceAuthentication contains SessionTranscript (the raw array),
     * not SessionTranscriptBytes (Tag 24 wrapped). The encoder must unwrap Tag 24 when the
     * verifier passes SessionTranscriptBytes.
     */
    @Test
    fun `unwraps Tag 24 from sessionTranscriptBytes before encoding`() {
        val docType = "org.iso.18013.5.1.mDL"
        val tag24Wrapped = ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeTag(24)
                gen.writeBinary(sessionTranscriptBytes)
            }
        }.toByteArray()

        val resultFromRaw = encoder.encode(
            sessionTranscriptBytes,
            docType,
            emptyDeviceNameSpacesBytes
        )
        val resultFromWrapped = encoder.encode(tag24Wrapped, docType, emptyDeviceNameSpacesBytes)

        assertThat(
            "Tag 24-wrapped input must produce same output as raw input",
            resultFromWrapped,
            equalTo(resultFromRaw)
        )
    }
}
