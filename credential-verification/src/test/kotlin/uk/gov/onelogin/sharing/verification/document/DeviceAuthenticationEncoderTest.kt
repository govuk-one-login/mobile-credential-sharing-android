package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import uk.gov.onelogin.sharing.verification.document.CoseSign1Stubs.emptyDeviceNameSpacesBytes
import uk.gov.onelogin.sharing.verification.document.CoseSign1Stubs.sessionTranscriptBytes

class DeviceAuthenticationEncoderTest {
    private val cborMapper = ObjectMapper(CBORFactory())
    private val encoder = DeviceAuthenticationEncoder()

    @Test
    fun `result is Tag 24 wrapping CBOR bstr`() {
        val result = encoder.encode(
            sessionTranscriptBytes,
            "org.iso.18013.5.1.mDL",
            emptyDeviceNameSpacesBytes
        )

        val outerNode = cborMapper.readTree(result)
        assertThat(outerNode.isBinary, equalTo(true))
    }

    @Test
    fun `inner array has 4 elements with correct structure`() {
        val docType = "org.iso.18013.5.1.mDL"

        val result = encoder.encode(
            sessionTranscriptBytes,
            docType,
            emptyDeviceNameSpacesBytes
        )

        val innerBytes = (cborMapper.readTree(result) as BinaryNode).binaryValue()
        val inner = cborMapper.readTree(innerBytes) as ArrayNode

        assertThat(inner.size(), equalTo(4))
        assertThat(inner[0].asText(), equalTo("DeviceAuthentication"))
        assertThat(inner[2].asText(), equalTo(docType))
    }

    @Test
    fun `SessionTranscript is embedded as decoded CBOR structure`() {
        val result = encoder.encode(
            sessionTranscriptBytes,
            "org.iso.18013.5.1.mDL",
            emptyDeviceNameSpacesBytes
        )

        val innerBytes = (cborMapper.readTree(result) as BinaryNode).binaryValue()
        val inner = cborMapper.readTree(innerBytes) as ArrayNode

        assertThat(inner[1].isArray, equalTo(true))
        assertThat(inner[1].size(), equalTo(3))
    }

    @Test
    fun `DeviceNameSpacesBytes is embedded as raw bytes`() {
        val result = encoder.encode(
            sessionTranscriptBytes,
            "org.iso.18013.5.1.mDL",
            emptyDeviceNameSpacesBytes
        )

        val innerBytes = (cborMapper.readTree(result) as BinaryNode).binaryValue()
        val inner = cborMapper.readTree(innerBytes) as ArrayNode

        assertThat(inner[3].isBinary, equalTo(true))
    }
}
