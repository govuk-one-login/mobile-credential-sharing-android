package uk.gov.onelogin.sharing.security

import java.security.interfaces.ECPublicKey
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.ALGORITHM
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateInvalidKeyPair
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateInvalidKeyPairWithValidAlgorithm
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateValidKeyPair
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.getKeyParameter

class SessionSecurityTest {
    @Test
    fun `generates valid public key`() {
        val publicKey = generateValidKeyPair()
        assertNotNull(publicKey)
    }

    @Test
    fun `generates public key using EC algorithm`() {
        val publicKey = generateValidKeyPair()
        assertEquals(ALGORITHM, publicKey?.algorithm)
    }

    @Test
    fun `generates key with secp256r1 curve`() {
        val publicKey = generateValidKeyPair()
        val ecPublicKey = publicKey as ECPublicKey

        val expectedParams = getKeyParameter()

        assertEquals(expectedParams.curve, ecPublicKey.params.curve)
    }

    @Test
    fun `returns null when NoSuchAlgorithmException is thrown`() {
        val publicKey = generateInvalidKeyPair()

        assertEquals(null, publicKey)
    }

    @Test
    fun `returns null when InvalidAlgorithmParameterException is thrown`() {
        val publicKey = generateInvalidKeyPairWithValidAlgorithm()

        assertEquals(null, publicKey)
    }
}
