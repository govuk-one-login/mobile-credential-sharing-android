package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import io.mockk.mockk
import java.io.ByteArrayOutputStream
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import uk.gov.onelogin.sharing.verification.trust.TrustVerifier

class BuildDeviceAuthenticationBytesTest {
    private val cborMapper = ObjectMapper(CBORFactory())
    private val deviceAuthVerifier = DeviceAuthVerifier(mockk<TrustVerifier>(relaxed = true))

    @Test
    fun `result is Tag 24 wrapping CBOR bstr`() {
        val sessionTranscript = buildSessionTranscript()
        val deviceNameSpacesBytes = buildEmptyDeviceNameSpacesBytes()

        val result = deviceAuthVerifier.buildDeviceAuthenticationBytes(
            sessionTranscript,
            "org.iso.18013.5.1.mDL",
            deviceNameSpacesBytes
        )

        // Decode the Tag 24 wrapper: should produce a bstr
        val outerNode = cborMapper.readTree(result)
        // Jackson CBOR reads Tag 24 + bstr as a BinaryNode
        assertThat(outerNode.isBinary, equalTo(true))
    }

    @Test
    fun `inner array has 4 elements with correct structure`() {
        val sessionTranscript = buildSessionTranscript()
        val deviceNameSpacesBytes = buildEmptyDeviceNameSpacesBytes()
        val docType = "org.iso.18013.5.1.mDL"

        val result = deviceAuthVerifier.buildDeviceAuthenticationBytes(
            sessionTranscript,
            docType,
            deviceNameSpacesBytes
        )

        val innerBytes = (cborMapper.readTree(result) as BinaryNode).binaryValue()
        val inner = cborMapper.readTree(innerBytes) as ArrayNode

        assertThat(inner.size(), equalTo(4))
        assertThat(inner[0].asText(), equalTo("DeviceAuthentication"))
        assertThat(inner[2].asText(), equalTo(docType))
    }

    @Test
    fun `SessionTranscript is embedded as decoded CBOR structure`() {
        val sessionTranscript = buildSessionTranscript()
        val deviceNameSpacesBytes = buildEmptyDeviceNameSpacesBytes()

        val result = deviceAuthVerifier.buildDeviceAuthenticationBytes(
            sessionTranscript,
            "org.iso.18013.5.1.mDL",
            deviceNameSpacesBytes
        )

        val innerBytes = (cborMapper.readTree(result) as BinaryNode).binaryValue()
        val inner = cborMapper.readTree(innerBytes) as ArrayNode

        // Element 1 should be the decoded SessionTranscript (an array)
        assertThat(inner[1].isArray, equalTo(true))
        assertThat(inner[1].size(), equalTo(3))
    }

    @Test
    fun `DeviceNameSpacesBytes is embedded as raw bytes`() {
        val sessionTranscript = buildSessionTranscript()
        val deviceNameSpacesBytes = buildEmptyDeviceNameSpacesBytes()

        val result = deviceAuthVerifier.buildDeviceAuthenticationBytes(
            sessionTranscript,
            "org.iso.18013.5.1.mDL",
            deviceNameSpacesBytes
        )

        val innerBytes = (cborMapper.readTree(result) as BinaryNode).binaryValue()
        val inner = cborMapper.readTree(innerBytes) as ArrayNode

        // Element 3 is the DeviceNameSpacesBytes (Tag 24 bstr - read as BinaryNode)
        assertThat(inner[3].isBinary, equalTo(true))
    }

    private fun buildSessionTranscript(): ByteArray {
        // SessionTranscript = [null, null, null]
        return ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeStartArray(null, 3)
                gen.writeNull()
                gen.writeNull()
                gen.writeNull()
                gen.writeEndArray()
            }
        }.toByteArray()
    }

    private fun buildEmptyDeviceNameSpacesBytes(): ByteArray {
        // Tag 24 wrapping an empty CBOR map
        val emptyMap = ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeStartObject(0)
                gen.writeEndObject()
            }
        }.toByteArray()

        return ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeTag(24)
                gen.writeBinary(emptyMap)
            }
        }.toByteArray()
    }
}
