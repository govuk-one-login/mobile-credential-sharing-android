package uk.gov.onelogin.sharing.verification.trust

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import io.mockk.mockk
import java.io.ByteArrayOutputStream
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError

class TrustVerifierImplTest {
    private val verifier = TrustVerifierImpl()

    @Test
    fun `verifyCOSESign1 with empty bytes throws MALFORMED_ISSUER_AUTH`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(byteArrayOf(), mockk())
        }

        assertThat(exception, hasError(VerificationError.MALFORMED_ISSUER_AUTH))
    }

    @Test
    fun `verifyCOSESign1 with detached payload throws INVALID_DEVICE_SIGNATURE`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(byteArrayOf(), mockk(), byteArrayOf())
        }

        assertThat(exception, hasError(VerificationError.INVALID_DEVICE_SIGNATURE))
    }

    @Test
    fun `decodeCOSESign1 with invalid CBOR throws MALFORMED_ISSUER_AUTH`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            CoseSign1Decoder.decode(byteArrayOf(0x01, 0x02))
        }

        assertThat(exception, hasError(VerificationError.MALFORMED_ISSUER_AUTH))
    }

    @Test
    fun `decodeCOSESign1 with wrong array size throws MALFORMED_ISSUER_AUTH`() {
        val cbor = buildCborArray(3)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            CoseSign1Decoder.decode(cbor)
        }

        assertThat(exception, hasError(VerificationError.MALFORMED_ISSUER_AUTH))
    }

    @Test
    fun `decodeCOSESign1 with no x5chain throws MALFORMED_ISSUER_AUTH`() {
        val cbor = buildCoseSign1WithoutX5Chain()
        val coseSign1 = CoseSign1Decoder.decode(cbor)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            CoseSign1Decoder.extractX5Chain(coseSign1)
        }

        assertThat(exception, hasError(VerificationError.MALFORMED_ISSUER_AUTH))
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

    private fun buildEmptyMapCbor(): ByteArray {
        val output = ByteArrayOutputStream()
        CBORFactory().createGenerator(output).use { gen ->
            gen.writeStartObject()
            gen.writeEndObject()
        }
        return output.toByteArray()
    }
}
