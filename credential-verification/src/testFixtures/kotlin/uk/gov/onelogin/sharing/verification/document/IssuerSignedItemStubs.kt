package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import uk.gov.onelogin.sharing.verification.document.IssuerSignedItemDto.Companion.KEY_DIGEST_ID
import uk.gov.onelogin.sharing.verification.document.IssuerSignedItemDto.Companion.KEY_ELEMENT_IDENTIFIER
import uk.gov.onelogin.sharing.verification.document.IssuerSignedItemDto.Companion.KEY_ELEMENT_VALUE
import uk.gov.onelogin.sharing.verification.document.IssuerSignedItemDto.Companion.KEY_RANDOM

object IssuerSignedItemStubs {

    private val cborMapper = ObjectMapper(CBORFactory())

    fun issuerSignedItemBytes(identifier: String, value: String): ByteArray {
        val inner = cborMapper.createObjectNode()
        inner.put(KEY_DIGEST_ID, 0)
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
