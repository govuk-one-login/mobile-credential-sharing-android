package uk.gov.onelogin.sharing.verification.trust.cose

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.verification.format.cose.CoseSign1
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError

@RunWith(Parameterized::class)
class CoseHeaderValidatorFailureTest(
    @Suppress("unused") private val name: String,
    private val coseSign1: CoseSign1,
    private val expectedError: VerificationError
) {
    private val validator = CoseHeaderValidator(SystemLogger())

    companion object {
        private val cborMapper = ObjectMapper(CBORFactory())

        private fun buildHeader(vararg entries: Pair<String, Long>): ByteArray {
            val node = cborMapper.createObjectNode()
            entries.forEach { (key, value) -> node.put(key, value) }
            return cborMapper.writeValueAsBytes(node)
        }

        private fun buildEmptyMap(): ByteArray =
            cborMapper.writeValueAsBytes(cborMapper.createObjectNode())

        private fun buildDuplicateKeyHeader(): ByteArray {
            val output = ByteArrayOutputStream()
            CBORFactory().createGenerator(output).use { gen ->
                gen.writeStartObject(2)
                gen.writeFieldName("1")
                gen.writeNumber(-7L)
                gen.writeFieldName("1")
                gen.writeNumber(-8L)
                gen.writeEndObject()
            }
            return output.toByteArray()
        }

        private fun coseSign1(
            protectedHeader: ByteArray = buildHeader("1" to -7L),
            unprotectedHeader: ByteArray? = buildEmptyMap()
        ) = CoseSign1(protectedHeader, unprotectedHeader, byteArrayOf(0x01), ByteArray(64))

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "alg absent from protected header",
                coseSign1(protectedHeader = buildEmptyMap()),
                VerificationError.INVALID_ISSUER_SIGNATURE
            ),
            arrayOf(
                "alg present in unprotected header",
                coseSign1(unprotectedHeader = buildHeader("1" to -7L)),
                VerificationError.INVALID_ISSUER_SIGNATURE
            ),
            arrayOf(
                "algorithm is not ES256",
                coseSign1(protectedHeader = buildHeader("1" to -35L)),
                VerificationError.INVALID_ISSUER_SIGNATURE
            ),
            arrayOf(
                "label appears in both headers",
                coseSign1(
                    protectedHeader = buildHeader("1" to -7L, "4" to 1L),
                    unprotectedHeader = buildHeader("4" to 2L)
                ),
                VerificationError.INVALID_ISSUER_SIGNATURE
            ),
            arrayOf(
                "duplicate keys in protected header",
                coseSign1(protectedHeader = buildDuplicateKeyHeader()),
                VerificationError.INVALID_ISSUER_SIGNATURE
            ),
            arrayOf(
                "propagates INVALID_DEVICE_SIGNATURE error",
                coseSign1(protectedHeader = buildEmptyMap()),
                VerificationError.INVALID_DEVICE_SIGNATURE
            )
        )
    }

    @Test
    fun `validate throws expected error`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.validate(coseSign1, expectedError)
        }
        assertThat(exception, hasError(expectedError))
    }
}

class CoseHeaderValidatorTest {
    private val validator = CoseHeaderValidator(SystemLogger())
    private val cborMapper = ObjectMapper(CBORFactory())

    @Test
    fun `valid ES256 header passes`() {
        val node = cborMapper.createObjectNode()
        node.put("1", -7L)
        val protectedHeader = cborMapper.writeValueAsBytes(node)
        val emptyMap = cborMapper.writeValueAsBytes(cborMapper.createObjectNode())
        val coseSign1 = CoseSign1(protectedHeader, emptyMap, byteArrayOf(0x01), ByteArray(64))

        validator.validate(coseSign1, VerificationError.INVALID_ISSUER_SIGNATURE)
    }
}
