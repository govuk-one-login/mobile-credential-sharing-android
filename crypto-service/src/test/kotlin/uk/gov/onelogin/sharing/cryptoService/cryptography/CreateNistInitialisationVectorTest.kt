package uk.gov.onelogin.sharing.cryptoService.cryptography

import kotlin.test.assertContentEquals
import org.junit.Test

class CreateNistInitialisationVectorTest {
    @Test
    fun `when supplying typical values, correct nist vector returned`() {
        assertContentEquals(
            VALID_BYTE_ARRAY,
            createNistInitialisationVector(
                1U,
                4U
            )
        )
    }

    @Test
    fun `when supplying max uint in message counter, correct nist vector returned`() {
        assertContentEquals(
            MAX_LIMIT_BYTE_ARRAY,
            createNistInitialisationVector(
                0U,
                UInt.MAX_VALUE
            )
        )
    }

    @Test
    fun `when supplying max uint in role, correct nist vector returned`() {
        assertContentEquals(
            MAX_LIMIT_BYTE_ARRAY_ROLE,
            createNistInitialisationVector(
                UInt.MAX_VALUE,
                0u
            )
        )
    }

    @Test
    fun `when supplying uint as zero for both, correct nist vector returned`() {
        assertContentEquals(
            ZERO_SUPPLIED_BYTE_ARRAY,
            createNistInitialisationVector(
                0u,
                0u
            )
        )
    }

    private companion object {
        val VALID_BYTE_ARRAY = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 4)

        val MAX_BYTE = 0xFF.toByte()
        val MAX_LIMIT_BYTE_ARRAY =
            byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, MAX_BYTE, MAX_BYTE, MAX_BYTE, MAX_BYTE)

        val MAX_LIMIT_BYTE_ARRAY_ROLE =
            byteArrayOf(0, 0, 0, 0, MAX_BYTE, MAX_BYTE, MAX_BYTE, MAX_BYTE, 0, 0, 0, 0)

        val ZERO_SUPPLIED_BYTE_ARRAY =
            byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    }
}
