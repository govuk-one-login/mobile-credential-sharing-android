package uk.gov.onelogin.sharing.security.cose

import java.math.BigInteger
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateValidKeyPair

class CoseKeyTest {

    @Test
    fun `should convert EcPublicKey to CoseKey with key type EC2`() {
        val keyPair = generateValidKeyPair()
        val coseKey = CoseKey.generateCoseKey(keyPair!!)

        assertEquals(Cose.KEY_TYPE_EC2, coseKey.keyType)
    }

    @Test
    fun `should convert EcPublicKey to CoseKey with curve P-256`() {
        val keyPair = generateValidKeyPair()
        val coseKey = CoseKey.generateCoseKey(keyPair!!)

        assertEquals(Cose.CURVE_P256, coseKey.curve)
    }

    @Test
    fun `should convert EcPublicKey to CoseKey with 32 byte length X coordinates`() {
        val keyPair = generateValidKeyPair()
        val coseKey = CoseKey.generateCoseKey(keyPair!!)

        assertEquals(32, coseKey.x.size)
    }

    @Test
    fun `should convert EcPublicKey to CoseKey with 32 byte length Y coordinates`() {
        val keyPair = generateValidKeyPair()
        val coseKey = CoseKey.generateCoseKey(keyPair!!)

        assertEquals(32, coseKey.y.size)
    }

    @Test
    fun `padEcCoordinatesTo32Bytes handles coordinate that is exactly 32 bytes`() {
        val thirtyTwoBytes = ByteArray(32) { 0x1A }
        val coord = BigInteger(1, thirtyTwoBytes)
        val result = CoseKey.padEcCoordinatesTo32Bytes(coord)
        assertEquals(32, result.size)
    }

    @Test
    fun `padEcCoordinatesTo32Bytes handles coordinate with a leading zero byte`() {
        val thirtyTwpBytesWithHighBit = ByteArray(32) { 0xFF.toByte() }
        val coord = BigInteger(1, thirtyTwpBytesWithHighBit)

        val result = CoseKey.padEcCoordinatesTo32Bytes(coord)

        assertArrayEquals(thirtyTwpBytesWithHighBit, result)
    }

    @Test
    fun `padEcCoordinatesTo32Bytes left pads byte array if less than 32 bytes`() {
        val smallByteArray = byteArrayOf(0x01, 0x02, 0x03)
        val coord = BigInteger(1, smallByteArray)

        val result = CoseKey.padEcCoordinatesTo32Bytes(coord)

        val expected = ByteArray(29) { 0 } + smallByteArray
        assertArrayEquals(expected, result)
    }
}
