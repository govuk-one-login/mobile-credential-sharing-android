package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory

object IssuerSignedItemStubs {

    private const val KEY_DIGEST_ID = "digestID"
    private const val KEY_RANDOM = "random"
    private const val KEY_ELEMENT_IDENTIFIER = "elementIdentifier"
    private const val KEY_ELEMENT_VALUE = "elementValue"

    private val cborMapper = ObjectMapper(CBORFactory())

    fun issuerSignedItemBytes(identifier: String, value: String, digestId: Long = 0): ByteArray {
        val inner = cborMapper.createObjectNode()
        inner.put(KEY_DIGEST_ID, digestId)
        inner.put(KEY_RANDOM, byteArrayOf(0x01))
        inner.put(KEY_ELEMENT_IDENTIFIER, identifier)
        inner.put(KEY_ELEMENT_VALUE, value)
        val innerBytes = cborMapper.writeValueAsBytes(inner)
        val output = java.io.ByteArrayOutputStream()
        val g = CBORFactory().createGenerator(output)
        g.writeTag(24)
        g.writeBinary(innerBytes)
        g.close()
        return output.toByteArray()
    }
}
