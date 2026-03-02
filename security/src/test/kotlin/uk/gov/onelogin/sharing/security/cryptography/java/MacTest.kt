package uk.gov.onelogin.sharing.security.cryptography.java

import kotlin.test.assertContentEquals
import org.junit.Test
import uk.gov.onelogin.sharing.security.cryptography.java.CryptoStub.BSB_BYTES
import uk.gov.onelogin.sharing.security.cryptography.java.CryptoStub.PRK_BYTES
import uk.gov.onelogin.sharing.security.cryptography.java.CryptoStub.VALID_MESSAGE_AUTHENTICATION_CODE_BYTES

class MacTest {
    @Test
    fun `generated message authentication code bytes matches stored test bytes`() {
        val messageAuthenticationCode = mac(
            PRK_BYTES,
            BSB_BYTES
        )

        assertContentEquals(messageAuthenticationCode, VALID_MESSAGE_AUTHENTICATION_CODE_BYTES)
    }

    @Test
    fun `when prk bytes are changed, message authentication code bytes do not match`() {
        val messageAuthenticationCode = mac(
            PRK_BYTES.copyOf().apply {
                set(0, 0x00)
            },
            BSB_BYTES
        )

        assert(!messageAuthenticationCode.contentEquals(VALID_MESSAGE_AUTHENTICATION_CODE_BYTES))
    }

    @Test
    fun `when bsb bytes are changed, message authentication code bytes do not match`() {
        val messageAuthenticationCode = mac(
            PRK_BYTES,
            BSB_BYTES.copyOf().apply {
                set(0, 0x00)
            }
        )

        assert(!messageAuthenticationCode.contentEquals(VALID_MESSAGE_AUTHENTICATION_CODE_BYTES))
    }
}
