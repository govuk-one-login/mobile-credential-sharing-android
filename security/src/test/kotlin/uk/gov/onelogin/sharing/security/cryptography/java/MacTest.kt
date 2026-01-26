package uk.gov.onelogin.sharing.security.cryptography.java

import org.junit.Test
import uk.gov.onelogin.sharing.security.util.getByteArrayFromFile
import kotlin.test.assertContentEquals

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

    private companion object {
        const val BINARY_PACKAGE_PATH =
            "src/testFixtures/resources/uk/gov/onelogin/sharing/security/cryptography/java/"

        val VALID_MESSAGE_AUTHENTICATION_CODE_BYTES = getByteArrayFromFile(
            BINARY_PACKAGE_PATH,
            "validMessageAuthenticationCode.bin"
        )

        val BSB_BYTES = getByteArrayFromFile(
            BINARY_PACKAGE_PATH,
            "testBSB.bin"
        )

        val PRK_BYTES = getByteArrayFromFile(
            BINARY_PACKAGE_PATH,
            "testPseudoRandomKey.bin"
        )
    }
}
