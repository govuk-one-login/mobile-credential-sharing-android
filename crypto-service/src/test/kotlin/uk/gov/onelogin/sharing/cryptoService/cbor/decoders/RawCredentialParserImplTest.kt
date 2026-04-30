package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.RawCredentialStub.validRawCredentialBytes
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.RawCredentialParserImpl
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.RawCredentialParsingException

class RawCredentialParserImplTest {

    private val parser = RawCredentialParserImpl()

    @Test
    fun `parses valid raw credential and extracts docType`() {
        val docType = "org.iso.18013.5.1.mDL"
        val result = parser.parse(validRawCredentialBytes)
        assertEquals(docType, result.msoDocType)
    }

    @Test
    fun `throws for empty bytes`() {
        assertThrows(RawCredentialParsingException::class.java) {
            parser.parse(byteArrayOf())
        }
    }

    @Test
    fun `throws when nameSpaces missing`() {
        val raw = buildCbor { gen ->
            gen.writeStartObject()
            gen.writeFieldName("issuerAuth")
            writeCoseSign1(gen)
            gen.writeEndObject()
        }
        assertThrows(RawCredentialParsingException::class.java) {
            parser.parse(raw)
        }
    }

    @Test
    fun `throws when issuerAuth missing`() {
        val raw = buildCbor { gen ->
            gen.writeStartObject()
            gen.writeFieldName("nameSpaces")
            gen.writeStartObject()
            gen.writeEndObject()
            gen.writeEndObject()
        }
        assertThrows(RawCredentialParsingException::class.java) {
            parser.parse(raw)
        }
    }

    @Test
    fun `throws when MSO has no docType`() {
        val msoBytes = buildCbor { gen ->
            gen.writeStartObject()
            gen.writeStringField("version", "1.0")
            gen.writeEndObject()
        }
        val tag24 = buildCbor { gen ->
            gen.writeTag(24)
            gen.writeBinary(msoBytes)
        }
        val raw = buildCbor { gen ->
            gen.writeStartObject()
            gen.writeFieldName("nameSpaces")
            gen.writeStartObject()
            gen.writeEndObject()
            gen.writeFieldName("issuerAuth")
            gen.writeStartArray()
            gen.writeBinary(byteArrayOf(0x01))
            gen.writeStartObject()
            gen.writeEndObject()
            gen.writeBinary(tag24)
            gen.writeBinary(byteArrayOf(0x02))
            gen.writeEndArray()
            gen.writeEndObject()
        }
        assertThrows(RawCredentialParsingException::class.java) {
            parser.parse(raw)
        }
    }

    private fun writeCoseSign1(gen: CBORGenerator) {
        val msoBytes = buildCbor { g ->
            g.writeStartObject()
            g.writeStringField("docType", "org.iso.18013.5.1.mDL")
            g.writeEndObject()
        }
        val tag24 = buildCbor { g ->
            g.writeTag(24)
            g.writeBinary(msoBytes)
        }
        gen.writeStartArray()
        gen.writeBinary(byteArrayOf(0x01))
        gen.writeStartObject()
        gen.writeEndObject()
        gen.writeBinary(tag24)
        gen.writeBinary(byteArrayOf(0x02))
        gen.writeEndArray()
    }

    private fun buildCbor(block: (CBORGenerator) -> Unit): ByteArray =
        ByteArrayOutputStream().use { out ->
            CBORFactory().createGenerator(out).use(block)
            out.toByteArray()
        }
}
