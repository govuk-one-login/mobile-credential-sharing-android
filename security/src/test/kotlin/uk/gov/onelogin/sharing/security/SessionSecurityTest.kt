package uk.gov.onelogin.sharing.security

import java.security.PublicKey
import java.security.interfaces.ECPublicKey
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.ALGORITHM
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.getKeyParameter

class SessionSecurityTest {
    private lateinit var sessionSecurity: SessionSecurityImpl
    private var publicKey: PublicKey? = null

    @Before
    fun setUp() {
        sessionSecurity = SessionSecurityImpl()
        publicKey = sessionSecurity.generateEcPublicKey()
    }

    @Test
    fun `generates public key`() {
        assertNotNull(publicKey)
    }

    @Test
    fun `generates public key using EC algorithm`() {
        assertEquals(ALGORITHM, publicKey?.algorithm)
    }

    @Test
    fun `generates key with secp256r1 curve`() {
        val ecPublicKey = publicKey as ECPublicKey

        val expectedParams = getKeyParameter()

        assertEquals(expectedParams.curve, ecPublicKey.params.curve)
    }
}
