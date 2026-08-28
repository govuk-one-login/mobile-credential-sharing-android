package uk.gov.onelogin.sharing.verification.cose.internal.decode

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.MalformedCoseSign1
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.UnsupportedAlgorithm

@RunWith(Parameterized::class)
internal class CoseHeaderValidatorFailureTest(
    @Suppress("unused") private val name: String,
    private val coseSign1: InternalCoseSign1,
    private val expectedFailure: CoseVerificationFailure
) {
    private val validator = CoseHeaderValidator()

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
        ) = InternalCoseSign1(protectedHeader, unprotectedHeader, byteArrayOf(0x01), ByteArray(64))

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "alg absent from protected header",
                coseSign1(protectedHeader = buildEmptyMap()),
                UnsupportedAlgorithm
            ),
            arrayOf(
                "alg present in unprotected header",
                coseSign1(unprotectedHeader = buildHeader("1" to -7L)),
                UnsupportedAlgorithm
            ),
            arrayOf(
                "algorithm is not ES256",
                coseSign1(protectedHeader = buildHeader("1" to -35L)),
                UnsupportedAlgorithm
            ),
            arrayOf(
                "label appears in both headers",
                coseSign1(
                    protectedHeader = buildHeader("1" to -7L, "4" to 1L),
                    unprotectedHeader = buildHeader("4" to 2L)
                ),
                UnsupportedAlgorithm
            ),
            arrayOf(
                "duplicate keys in protected header",
                coseSign1(protectedHeader = buildDuplicateKeyHeader()),
                MalformedCoseSign1
            ),
            arrayOf(
                "malformed protected header bytes",
                coseSign1(protectedHeader = byteArrayOf(0xFF.toByte(), 0xFE.toByte())),
                MalformedCoseSign1
            )
        )
    }

    @Test
    fun `validate throws expected failure`() {
        assertThrows(expectedFailure::class.java) {
            validator.validate(coseSign1)
        }
    }
}

class CoseHeaderValidatorTest {
    private val validator = CoseHeaderValidator()
    private val cborMapper = ObjectMapper(CBORFactory())

    @Test
    fun `valid ES256 header passes`() {
        val node = cborMapper.createObjectNode()
        node.put("1", -7L)
        val protectedHeader = cborMapper.writeValueAsBytes(node)
        val emptyMap = cborMapper.writeValueAsBytes(cborMapper.createObjectNode())
        val coseSign1 =
            InternalCoseSign1(protectedHeader, emptyMap, byteArrayOf(0x01), ByteArray(64))

        validator.validate(coseSign1)
    }
}
