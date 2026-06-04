package uk.gov.onelogin.sharing.verification.trust

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import io.mockk.mockk
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.cert.X509Certificate
import java.security.spec.ECGenParameterSpec
import java.util.Date
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
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
        // CBOR array with 3 elements
        val cbor = buildCborArray(3)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            CoseSign1Decoder.decode(cbor)
        }

        assertThat(exception, hasError(VerificationError.MALFORMED_ISSUER_AUTH))
    }

    @Test
    fun `decodeCOSESign1 with no x5chain throws MALFORMED_ISSUER_AUTH`() {
        // Valid 4-element array but no x5chain in headers
        val cbor = buildCoseSign1WithoutX5Chain()
        val coseSign1 = CoseSign1Decoder.decode(cbor)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            CoseSign1Decoder.extractX5Chain(coseSign1)
        }

        assertThat(exception, hasError(VerificationError.MALFORMED_ISSUER_AUTH))
    }

    @Test
    fun `orderCertificates with single cert returns it unchanged`() {
        val cert = mockk<X509Certificate>(relaxed = true)
        val result = verifier.orderCertificates(listOf(cert))
        assertThat(result.size, equalTo(1))
        assertThat(result[0], equalTo(cert))
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
            // protected header (empty map as bstr)
            gen.writeBinary(buildEmptyMapCbor())
            // unprotected header (empty map)
            gen.writeStartObject()
            gen.writeEndObject()
            // payload
            gen.writeBinary(byteArrayOf(0x01))
            // signature
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
