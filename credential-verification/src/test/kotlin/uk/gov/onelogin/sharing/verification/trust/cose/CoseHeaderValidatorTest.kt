package uk.gov.onelogin.sharing.verification.trust.cose

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.verification.format.cose.CoseSign1
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError

class CoseHeaderValidatorTest {
    private val validator = CoseHeaderValidator(SystemLogger())
    private val cborMapper = ObjectMapper(CBORFactory())
    private val error = VerificationError.INVALID_ISSUER_SIGNATURE

    private fun buildHeader(vararg entries: Pair<String, Long>): ByteArray {
        val node = cborMapper.createObjectNode()
        entries.forEach { (key, value) -> node.put(key, value) }
        return cborMapper.writeValueAsBytes(node)
    }

    private fun buildEmptyMap(): ByteArray =
        cborMapper.writeValueAsBytes(cborMapper.createObjectNode())

    private fun coseSign1(
        protectedHeader: ByteArray = buildHeader("1" to -7L),
        unprotectedHeader: ByteArray? = buildEmptyMap()
    ) = CoseSign1(protectedHeader, unprotectedHeader, byteArrayOf(0x01), ByteArray(64))

    @Test
    fun `valid ES256 header passes`() {
        validator.validate(coseSign1(), error)
    }

    @Test
    fun `throws when alg absent from protected header`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.validate(coseSign1(protectedHeader = buildEmptyMap()), error)
        }
        assertThat(exception, hasError(error))
    }

    @Test
    fun `throws when alg present in unprotected header`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.validate(
                coseSign1(unprotectedHeader = buildHeader("1" to -7L)),
                error
            )
        }
        assertThat(exception, hasError(error))
    }

    @Test
    fun `throws when algorithm is not ES256`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.validate(coseSign1(protectedHeader = buildHeader("1" to -35L)), error)
        }
        assertThat(exception, hasError(error))
    }

    @Test
    fun `throws when label appears in both headers`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.validate(
                coseSign1(
                    protectedHeader = buildHeader("1" to -7L, "4" to 1L),
                    unprotectedHeader = buildHeader("4" to 2L)
                ),
                error
            )
        }
        assertThat(exception, hasError(error))
    }

    @Test
    fun `throws with correct error type when INVALID_DEVICE_SIGNATURE specified`() {
        val deviceError = VerificationError.INVALID_DEVICE_SIGNATURE
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.validate(coseSign1(protectedHeader = buildEmptyMap()), deviceError)
        }
        assertThat(exception, hasError(deviceError))
    }
}
