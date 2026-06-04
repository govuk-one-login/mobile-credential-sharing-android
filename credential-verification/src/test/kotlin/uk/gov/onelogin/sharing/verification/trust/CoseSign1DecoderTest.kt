package uk.gov.onelogin.sharing.verification.trust

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError

class CoseSign1DecoderTest {

    @Test
    fun `decode with invalid CBOR throws MALFORMED_ISSUER_AUTH`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            CoseSign1Decoder.decode(byteArrayOf(0x01, 0x02))
        }

        assertThat(exception, hasError(VerificationError.MALFORMED_ISSUER_AUTH))
    }

    @Test
    fun `decode with wrong array size throws MALFORMED_ISSUER_AUTH`() {
        val cbor = buildCborArray(3)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            CoseSign1Decoder.decode(cbor)
        }

        assertThat(exception, hasError(VerificationError.MALFORMED_ISSUER_AUTH))
    }

    @Test
    fun `decode with valid COSE_Sign1 returns CoseSign1`() {
        val cbor = buildValidCoseSign1()

        val result = CoseSign1Decoder.decode(cbor)

        assertThat(result.protectedHeader, notNullValue())
        assertThat(result.signature, notNullValue())
    }

    @Test
    fun `extractX5Chain with no x5chain throws MALFORMED_ISSUER_AUTH`() {
        val cbor = buildCoseSign1WithoutX5Chain()
        val coseSign1 = CoseSign1Decoder.decode(cbor)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            CoseSign1Decoder.extractX5Chain(coseSign1)
        }

        assertThat(exception, hasError(VerificationError.MALFORMED_ISSUER_AUTH))
    }

    @Test
    fun `extractX5Chain with single cert in unprotected header returns it`() {
        val certBytes = byteArrayOf(0x30, 0x01, 0x00)
        val cbor = buildCoseSign1WithX5Chain(certBytes)
        val coseSign1 = CoseSign1Decoder.decode(cbor)

        val result = CoseSign1Decoder.extractX5Chain(coseSign1)

        assertThat(result.size, equalTo(1))
        assertThat(result[0], equalTo(certBytes))
    }

    private fun buildCborArray(size: Int): ByteArray {
        val output = ByteArrayOutputStream()
        CBORFactory().createGenerator(output).use { gen ->
            @Suppress("DEPRECATION")
            gen.writeStartArray(size)
            repeat(size) { gen.writeBinary(byteArrayOf(0x00)) }
            gen.writeEndArray()
        }
        return output.toByteArray()
    }

    private fun buildValidCoseSign1(): ByteArray {
        val output = ByteArrayOutputStream()
        CBORFactory().createGenerator(output).use { gen ->
            @Suppress("DEPRECATION")
            gen.writeStartArray(4)
            gen.writeBinary(buildEmptyMapCbor())
            gen.writeBinary(buildEmptyMapCbor())
            gen.writeBinary(byteArrayOf(0x01))
            gen.writeBinary(byteArrayOf(0x02))
            gen.writeEndArray()
        }
        return output.toByteArray()
    }

    private fun buildCoseSign1WithoutX5Chain(): ByteArray {
        val output = ByteArrayOutputStream()
        CBORFactory().createGenerator(output).use { gen ->
            @Suppress("DEPRECATION")
            gen.writeStartArray(4)
            gen.writeBinary(buildEmptyMapCbor())
            gen.writeStartObject()
            gen.writeEndObject()
            gen.writeBinary(byteArrayOf(0x01))
            gen.writeBinary(byteArrayOf(0x02))
            gen.writeEndArray()
        }
        return output.toByteArray()
    }

    private fun buildCoseSign1WithX5Chain(certBytes: ByteArray): ByteArray {
        val unprotectedHeader = buildX5ChainMapCbor(certBytes)
        val output = ByteArrayOutputStream()
        CBORFactory().createGenerator(output).use { gen ->
            @Suppress("DEPRECATION")
            gen.writeStartArray(4)
            gen.writeBinary(buildEmptyMapCbor())
            gen.writeBinary(unprotectedHeader)
            gen.writeBinary(byteArrayOf(0x01))
            gen.writeBinary(byteArrayOf(0x02))
            gen.writeEndArray()
        }
        return output.toByteArray()
    }

    private fun buildX5ChainMapCbor(certBytes: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        CBORFactory().createGenerator(output).use { gen ->
            gen.writeStartObject()
            gen.writeFieldName("33")
            gen.writeBinary(certBytes)
            gen.writeEndObject()
        }
        return output.toByteArray()
    }

    private fun buildEmptyMapCbor(): ByteArray {
        val output = ByteArrayOutputStream()
        CBORFactory().createGenerator(output).use { gen ->
            gen.writeStartObject()
            gen.writeEndObject()
        }
        return output.toByteArray()
    }
}
