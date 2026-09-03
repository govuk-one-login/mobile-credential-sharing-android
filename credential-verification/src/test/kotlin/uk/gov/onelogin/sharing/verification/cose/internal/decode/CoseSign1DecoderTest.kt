package uk.gov.onelogin.sharing.verification.cose.internal.decode

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.MalformedCoseSign1
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.MissingX5Chain

@RunWith(Parameterized::class)
class CoseSign1DecoderDecodeErrorTest(
    @Suppress("unused") private val name: String,
    private val input: ByteArray
) {
    private val decoder = CoseSign1Decoder()

    companion object {
        private val cborFactory = CBORFactory()

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf("invalid CBOR", byteArrayOf(0x01, 0x02)),
            arrayOf("wrong array size", buildCborArray(3)),
            arrayOf("extra array elements", buildCborArray(5)),
            arrayOf("non-binary signature", buildCoseSign1Cbor(signature = "text")),
            arrayOf("non-binary protected header", buildCoseSign1Cbor(protectedHeader = "text")),
            arrayOf("wrapped in tag 18", buildCoseSign1Cbor(tag = 18)),
            arrayOf("wrapped in tag 24", buildCoseSign1Cbor(tag = 24)),
            arrayOf("trailing garbage bytes", buildCoseSign1Cbor() + byteArrayOf(0xFF.toByte())),
            arrayOf(
                "truncated array mid-stream",
                byteArrayOf(0x84.toByte(), 0x41.toByte(), 0x01.toByte())
            )
        )

        private fun buildCborArray(size: Int): ByteArray {
            val output = ByteArrayOutputStream()
            cborFactory.createGenerator(output).use { gen ->
                @Suppress("DEPRECATION")
                gen.writeStartArray(size)
                repeat(size) { gen.writeBinary(byteArrayOf(0x00)) }
                gen.writeEndArray()
            }
            return output.toByteArray()
        }

        private fun buildCoseSign1Cbor(
            protectedHeader: Any = byteArrayOf(0x01),
            signature: Any = byteArrayOf(0x02),
            tag: Int? = null
        ): ByteArray {
            val output = ByteArrayOutputStream()
            cborFactory.createGenerator(output).use { gen ->
                tag?.let { gen.writeTag(it) }
                @Suppress("DEPRECATION")
                gen.writeStartArray(4)
                writeElement(gen, protectedHeader)
                gen.writeStartObject()
                gen.writeEndObject()
                gen.writeBinary(byteArrayOf(0x01))
                writeElement(gen, signature)
                gen.writeEndArray()
            }
            return output.toByteArray()
        }

        private fun writeElement(gen: com.fasterxml.jackson.core.JsonGenerator, value: Any) {
            when (value) {
                is ByteArray -> gen.writeBinary(value)
                is String -> gen.writeString(value)
            }
        }
    }

    @Test
    fun `decode throws MalformedCoseSign1`() {
        assertThrows(MalformedCoseSign1::class.java) {
            decoder.decode(input)
        }
    }
}

class CoseSign1DecoderTest {
    private val cborFactory = CBORFactory()
    private val cborMapper = ObjectMapper(cborFactory)
    private val decoder = CoseSign1Decoder()

    @Test
    fun `decode with valid attached COSE_Sign1 returns Attached mode`() {
        val cbor = buildCoseSign1Cbor()

        val result = decoder.decode(cbor)

        assertThat(result.payloadMode, equalTo(InternalCoseSign1.PayloadMode.ATTACHED))
        assertThat(result.payload, notNullValue())
    }

    @Test
    fun `decode with null payload returns Detached mode`() {
        val cbor = buildCoseSign1Cbor(payload = null)

        val result = decoder.decode(cbor)

        assertThat(result.payloadMode, equalTo(InternalCoseSign1.PayloadMode.DETACHED))
        assertThat(result.payload, equalTo(null))
    }

    @Test
    fun `extractX5Chain with no x5chain throws MissingX5Chain`() {
        val cbor = buildCoseSign1Cbor()
        val coseSign1 = decoder.decode(cbor)

        assertThrows(MissingX5Chain::class.java) {
            decoder.extractX5Chain(coseSign1)
        }
    }

    @Test
    fun `extractX5Chain with single cert in unprotected header returns it`() {
        val certBytes = byteArrayOf(0x30, 0x01, 0x00)
        val cbor = buildCoseSign1Cbor(unprotectedHeaderMap = buildX5ChainMapCbor(certBytes))
        val coseSign1 = decoder.decode(cbor)

        val result = decoder.extractX5Chain(coseSign1)

        assertThat(result.size, equalTo(1))
        assertThat(result[0], equalTo(certBytes))
    }

    @Test
    fun `extractX5Chain with array of certs returns all`() {
        val cert1 = byteArrayOf(0x30, 0x01)
        val cert2 = byteArrayOf(0x30, 0x02)
        val cbor = buildCoseSign1Cbor(unprotectedHeaderMap = buildX5ChainArrayMapCbor(cert1, cert2))
        val coseSign1 = decoder.decode(cbor)

        val result = decoder.extractX5Chain(coseSign1)

        assertThat(result.size, equalTo(2))
        assertThat(result[0], equalTo(cert1))
        assertThat(result[1], equalTo(cert2))
    }

    @Test
    fun `extractX5Chain falls back to protected header`() {
        val certBytes = byteArrayOf(0x30, 0x01, 0x00)
        val coseSign1 = InternalCoseSign1(
            protectedHeader = buildX5ChainMapCbor(certBytes),
            unprotectedHeader = buildEmptyMapCbor(),
            payload = byteArrayOf(0x01),
            signature = byteArrayOf(0x02),
            payloadMode = InternalCoseSign1.PayloadMode.ATTACHED
        )

        val result = decoder.extractX5Chain(coseSign1)

        assertThat(result.size, equalTo(1))
        assertThat(result[0], equalTo(certBytes))
    }

    @Test
    fun `unprotected header is preserved as raw bytes`() {
        val cbor = buildCoseSign1Cbor()

        val result = decoder.decode(cbor)

        assertThat(result.unprotectedHeader, notNullValue())
        val firstByte = result.unprotectedHeader!![0].toInt() and 0xFF
        val isMap = firstByte == 0xa0 || firstByte == 0xbf
        assertThat(isMap, equalTo(true))
    }

    private fun buildCoseSign1Cbor(
        unprotectedHeaderMap: ByteArray? = null,
        payload: ByteArray? = byteArrayOf(0x01)
    ): ByteArray {
        val output = ByteArrayOutputStream()
        cborFactory.createGenerator(output).use { gen ->
            @Suppress("DEPRECATION")
            gen.writeStartArray(4)
            gen.writeBinary(byteArrayOf(0xa0.toByte())) // Empty protected map

            if (unprotectedHeaderMap != null) {
                val node = cborMapper.readTree(unprotectedHeaderMap)
                cborMapper.writeTree(gen, node)
            } else {
                gen.writeStartObject()
                gen.writeEndObject()
            }

            if (payload != null) {
                gen.writeBinary(payload)
            } else {
                gen.writeNull()
            }
            gen.writeBinary(byteArrayOf(0x02))
            gen.writeEndArray()
        }
        return output.toByteArray()
    }

    private fun buildX5ChainMapCbor(certBytes: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        cborFactory.createGenerator(output).use { gen ->
            gen.writeStartObject()
            gen.writeFieldName("33")
            gen.writeBinary(certBytes)
            gen.writeEndObject()
        }
        return output.toByteArray()
    }

    private fun buildX5ChainArrayMapCbor(vararg certs: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        cborFactory.createGenerator(output).use { gen ->
            gen.writeStartObject()
            gen.writeFieldName("33")
            @Suppress("DEPRECATION")
            gen.writeStartArray(certs.size)
            certs.forEach { gen.writeBinary(it) }
            gen.writeEndArray()
            gen.writeEndObject()
        }
        return output.toByteArray()
    }

    private fun buildEmptyMapCbor(): ByteArray {
        val output = ByteArrayOutputStream()
        cborFactory.createGenerator(output).use { gen ->
            gen.writeStartObject()
            gen.writeEndObject()
        }
        return output.toByteArray()
    }
}
