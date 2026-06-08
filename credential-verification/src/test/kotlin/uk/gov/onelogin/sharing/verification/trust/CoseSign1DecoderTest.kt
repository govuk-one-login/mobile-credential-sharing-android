package uk.gov.onelogin.sharing.verification.trust

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import io.mockk.mockk
import java.io.ByteArrayOutputStream
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import uk.gov.logging.api.v2.Logger
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.verification.format.cose.CoseSign1
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError

@RunWith(Parameterized::class)
class CoseSign1DecoderDecodeErrorTest(
    @Suppress("unused") private val name: String,
    private val input: ByteArray
) {
    private val decoder = CoseSign1Decoder(SystemLogger())

    companion object {
        private val cborFactory = CBORFactory()

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf("invalid CBOR", byteArrayOf(0x01, 0x02)),
            arrayOf("wrong array size", buildCborArray(3)),
            arrayOf("non-binary signature", buildCoseSign1Cbor(signature = "text")),
            arrayOf("non-binary protected header", buildCoseSign1Cbor(protectedHeader = "text"))
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
            signature: Any = byteArrayOf(0x02)
        ): ByteArray {
            val output = ByteArrayOutputStream()
            cborFactory.createGenerator(output).use { gen ->
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
    fun `decode throws MALFORMED_ISSUER_AUTH`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            decoder.decode(input)
        }

        assertThat(exception, hasError(VerificationError.MALFORMED_ISSUER_AUTH))
    }
}

class CoseSign1DecoderTest {
    private val cborFactory = CBORFactory()
    private val decoder = CoseSign1Decoder(SystemLogger())

    @Test
    fun `decode with valid COSE_Sign1 returns CoseSign1`() {
        val cbor = buildCoseSign1Cbor()

        val result = decoder.decode(cbor)

        assertThat(result.protectedHeader, notNullValue())
        assertThat(result.signature, notNullValue())
    }

    @Test
    fun `decode with null payload returns CoseSign1 with null payload`() {
        val cbor = buildCoseSign1Cbor(payload = null)

        val result = decoder.decode(cbor)

        assertThat(result.payload, equalTo(null))
    }

    @Test
    fun `extractX5Chain with no x5chain throws MALFORMED_ISSUER_AUTH`() {
        val cbor = buildCoseSign1Cbor()
        val coseSign1 = decoder.decode(cbor)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            decoder.extractX5Chain(coseSign1)
        }

        assertThat(exception, hasError(VerificationError.MALFORMED_ISSUER_AUTH))
    }

    @Test
    fun `extractX5Chain with single cert in unprotected header returns it`() {
        val certBytes = byteArrayOf(0x30, 0x01, 0x00)
        val cbor = buildCoseSign1Cbor(unprotectedHeader = buildX5ChainMapCbor(certBytes))
        val coseSign1 = decoder.decode(cbor)

        val result = decoder.extractX5Chain(coseSign1)

        assertThat(result.size, equalTo(1))
        assertThat(result[0], equalTo(certBytes))
    }

    @Test
    fun `extractX5Chain with array of certs returns all`() {
        val cert1 = byteArrayOf(0x30, 0x01)
        val cert2 = byteArrayOf(0x30, 0x02)
        val cbor = buildCoseSign1Cbor(unprotectedHeader = buildX5ChainArrayMapCbor(cert1, cert2))
        val coseSign1 = decoder.decode(cbor)

        val result = decoder.extractX5Chain(coseSign1)

        assertThat(result.size, equalTo(2))
        assertThat(result[0], equalTo(cert1))
        assertThat(result[1], equalTo(cert2))
    }

    @Test
    fun `extractX5Chain falls back to protected header`() {
        val certBytes = byteArrayOf(0x30, 0x01, 0x00)
        val coseSign1 = CoseSign1(
            protectedHeader = buildX5ChainMapCbor(certBytes),
            unprotectedHeader = buildEmptyMapCbor(),
            payload = byteArrayOf(0x01),
            signature = byteArrayOf(0x02)
        )

        val result = decoder.extractX5Chain(coseSign1)

        assertThat(result.size, equalTo(1))
        assertThat(result[0], equalTo(certBytes))
    }

    @Test
    fun `extractX5Chain with null unprotected header falls back to protected header`() {
        val certBytes = byteArrayOf(0x30, 0x01, 0x00)
        val coseSign1 = CoseSign1(
            protectedHeader = buildX5ChainMapCbor(certBytes),
            unprotectedHeader = null,
            payload = byteArrayOf(0x01),
            signature = byteArrayOf(0x02)
        )

        val result = decoder.extractX5Chain(coseSign1)

        assertThat(result.size, equalTo(1))
        assertThat(result[0], equalTo(certBytes))
    }

    private fun buildCoseSign1Cbor(
        unprotectedHeader: ByteArray? = null,
        payload: ByteArray? = byteArrayOf(0x01)
    ): ByteArray {
        val output = ByteArrayOutputStream()
        cborFactory.createGenerator(output).use { gen ->
            @Suppress("DEPRECATION")
            gen.writeStartArray(4)
            gen.writeBinary(buildEmptyMapCbor())
            if (unprotectedHeader != null) {
                gen.writeBinary(unprotectedHeader)
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
