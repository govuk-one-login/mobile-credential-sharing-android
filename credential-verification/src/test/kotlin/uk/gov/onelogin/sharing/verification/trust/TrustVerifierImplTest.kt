package uk.gov.onelogin.sharing.verification.trust

import io.mockk.mockk
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.document.result.VerificationResultMatchers.hasError

class TrustVerifierImplTest {
    private val verifier by lazy {
        TrustVerifierImpl()
    }

    @Test
    fun `Verifying an X509 certificate throws a verification failure`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(byteArrayOf(), mockk())
        }

        assertThat(
            exception,
            hasError(VerificationError.MALFORMED_ISSUER_AUTH)
        )
    }

    @Test
    fun `Verifying ByteArray data throws a verification failure`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(byteArrayOf(), mockk(), byteArrayOf())
        }

        assertThat(
            exception,
            hasError(VerificationError.INVALID_DEVICE_SIGNATURE)
        )
    }

    @Test
    fun `decodeCOSESign1() isn't implemented`() {
        val exception = assertThrows(IllegalStateException::class.java) {
            verifier.decodeCOSESign1()
        }

        assertThat(
            exception.message,
            equalTo("This function isn't implemented yet")
        )
    }

    @Test
    fun `verifyCertificateChain() isn't implemented`() {
        val exception = assertThrows(IllegalStateException::class.java) {
            verifier.verifyCertificateChain()
        }

        assertThat(
            exception.message,
            equalTo("This function isn't implemented yet")
        )
    }
}
