package uk.gov.onelogin.sharing.verification.trust

import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError

class TrustVerifierImplTest {
    private val verifier = TrustVerifierImpl()

    @Test
    fun `verifyCOSESign1 with empty bytes throws MALFORMED_ISSUER_AUTH`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(byteArrayOf(), mockk())
        }

        assertThat(exception, hasError(VerificationError.MALFORMED_ISSUER_AUTH))
    }

    @Test
    fun `verifyCOSESign1 with detached payload throws INVALID_DEVICE_SIGNATURE`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(byteArrayOf(), mockk(), byteArrayOf())
        }

        assertThat(exception, hasError(VerificationError.INVALID_DEVICE_SIGNATURE))
    }
}
