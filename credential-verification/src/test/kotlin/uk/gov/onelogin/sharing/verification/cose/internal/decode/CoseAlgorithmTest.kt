package uk.gov.onelogin.sharing.verification.cose.internal.decode

import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CoseAlgorithmTest {

    private fun generateEcKeyPair(curveName: String) = KeyPairGenerator.getInstance("EC").apply {
        initialize(java.security.spec.ECGenParameterSpec(curveName))
    }.generateKeyPair()

    @Test
    fun `fromId returns ES256 for id -7`() {
        assertEquals(CoseAlgorithm.ES256, CoseAlgorithm.fromId(-7L))
    }

    @Test
    fun `fromId returns ES384 for id -35`() {
        assertEquals(CoseAlgorithm.ES384, CoseAlgorithm.fromId(-35L))
    }

    @Test
    fun `fromId returns null for unknown ID`() {
        assertNull(CoseAlgorithm.fromId(0L))
        assertNull(CoseAlgorithm.fromId(-36L))
        assertNull(CoseAlgorithm.fromId(7L))
    }

    @Test
    fun `ES256 parameters match specification`() {
        assertEquals("SHA256withECDSA", CoseAlgorithm.ES256.jcaAlgorithmName)
        assertEquals(64, CoseAlgorithm.ES256.signatureLength)
        assertEquals(256, CoseAlgorithm.ES256.curveBitLength)
    }

    @Test
    fun `ES384 parameters match specification`() {
        assertEquals("SHA384withECDSA", CoseAlgorithm.ES384.jcaAlgorithmName)
        assertEquals(96, CoseAlgorithm.ES384.signatureLength)
        assertEquals(384, CoseAlgorithm.ES384.curveBitLength)
    }

    @Test
    fun `Scenario - 384-bit EC Key maps correctly to ES384`() {
        // Simulating the P-384 key generation as done for the DVS DEV leaf certificate
        val keyPair = generateEcKeyPair("secp384r1")
        val publicKey = keyPair.public as ECPublicKey

        val curveSize = publicKey.params.order.bitLength()

        // Verify the bit length maps to the ES384 curve definition
        val resolvedAlgorithm = CoseAlgorithm.entries.firstOrNull { it.curveBitLength == curveSize }

        assertEquals(CoseAlgorithm.ES384, resolvedAlgorithm)
        assertEquals(384, curveSize)
    }

    @Test
    fun `Scenario - 256-bit EC Key maps correctly to ES256`() {
        // Simulating a standard P-256 mDoc reader key
        val keyPair = generateEcKeyPair("secp256r1")
        val publicKey = keyPair.public as ECPublicKey

        val curveSize = publicKey.params.order.bitLength()

        // Verify the bit length maps to the ES256 curve definition
        val resolvedAlgorithm = CoseAlgorithm.entries.firstOrNull { it.curveBitLength == curveSize }

        assertEquals(CoseAlgorithm.ES256, resolvedAlgorithm)
        assertEquals(256, curveSize)
    }
}
