package uk.gov.onelogin.sharing.verification.cose.internal.decode

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
    fun `unprotected header is preserved as raw bytes`() {
        val cbor = buildCoseSign1Cbor()

        val result = decoder.decode(cbor)

        assertThat(result.unprotectedHeader, notNullValue())
        val firstByte = result.unprotectedHeader!![0].toInt() and 0xFF
        val isMap = firstByte == 0xa0 || firstByte == 0xbf
        assertThat(isMap, equalTo(true))
    }

    private fun buildCoseSign1Cbor(payload: ByteArray? = byteArrayOf(0x01)): ByteArray {
        val output = ByteArrayOutputStream()
        cborFactory.createGenerator(output).use { gen ->
            @Suppress("DEPRECATION")
            gen.writeStartArray(4)
            gen.writeBinary(byteArrayOf(0xa0.toByte())) // Empty protected map

            gen.writeStartObject()
            gen.writeEndObject()

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
}
