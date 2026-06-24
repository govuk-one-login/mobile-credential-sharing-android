package uk.gov.onelogin.sharing.verification.document

import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError
import uk.gov.onelogin.sharing.verification.format.document.validity.CertificateValidityPeriod
import uk.gov.onelogin.sharing.verification.format.document.validity.ValidityInfo

@OptIn(ExperimentalTime::class)
class ValidityInfoVerifierImplTest {

    private val now = Clock.System.now()
    private val clock = object : Clock {
        override fun now(): Instant = now
    }
    private val verifier = ValidityInfoVerifierImpl(clock)

    private val validityPeriod = CertificateValidityPeriod(
        notBefore = now - 30.minutes,
        notAfter = now + 30.minutes
    )

    private fun validityInfo() = ValidityInfo(
        signed = now - 5.minutes,
        validFrom = now - 3.minutes,
        validUntil = now + 10.minutes
    )

    @Test
    fun `verify does not throw for valid validity info`() {
        verifier.verify(validityPeriod, validityInfo())
    }

    @Test
    fun `verify throws MALFORMED_MSO when validUntil equals validFrom`() {
        val info = validityInfo().copy(validUntil = validityInfo().validFrom)
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verify(validityPeriod, info)
        }
        assertThat(exception, hasError(VerificationError.MALFORMED_MSO))
    }

    @Test
    fun `verify throws MALFORMED_MSO when validUntil is before validFrom`() {
        val info = validityInfo().copy(validUntil = validityInfo().validFrom - 1.minutes)
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verify(validityPeriod, info)
        }
        assertThat(exception, hasError(VerificationError.MALFORMED_MSO))
    }

    @Test
    fun `verify throws VALIDITY_SIGNED_OUT_OF_RANGE when signed is in the future`() {
        val info = validityInfo().copy(signed = now + 1.minutes)
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verify(validityPeriod, info)
        }
        assertThat(exception, hasError(VerificationError.VALIDITY_SIGNED_OUT_OF_RANGE))
    }

    @Test
    fun `verify throws VALIDITY_SIGNED_OUT_OF_RANGE when signed is before certificate notBefore`() {
        val info = validityInfo().copy(signed = validityPeriod.notBefore - 1.minutes)
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verify(validityPeriod, info)
        }
        assertThat(exception, hasError(VerificationError.VALIDITY_SIGNED_OUT_OF_RANGE))
    }

    @Test
    fun `verify throws VALIDITY_SIGNED_OUT_OF_RANGE when signed is after certificate notAfter`() {
        val period = validityPeriod.copy(notAfter = now - 10.minutes)
        val info = validityInfo().copy(signed = now - 8.minutes)
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verify(period, info)
        }
        assertThat(exception, hasError(VerificationError.VALIDITY_SIGNED_OUT_OF_RANGE))
    }

    @Test
    fun `verify throws VALIDITY_FROM_OUT_OF_RANGE when validFrom is before signed`() {
        val info = validityInfo().copy(
            signed = now - 5.minutes,
            validFrom = now - 6.minutes
        )
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verify(validityPeriod, info)
        }
        assertThat(exception, hasError(VerificationError.VALIDITY_FROM_OUT_OF_RANGE))
    }

    @Test
    fun `verify throws VALIDITY_FROM_OUT_OF_RANGE when validFrom is in the future`() {
        val info = validityInfo().copy(validFrom = now + 1.minutes)
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verify(validityPeriod, info)
        }
        assertThat(exception, hasError(VerificationError.VALIDITY_FROM_OUT_OF_RANGE))
    }

    @Test
    fun `verify throws VALIDITY_UNTIL_EXPIRED when validUntil is in the past`() {
        val info = validityInfo().copy(validUntil = now - 1.minutes)
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verify(validityPeriod, info)
        }
        assertThat(exception, hasError(VerificationError.VALIDITY_UNTIL_EXPIRED))
    }

    @Test
    fun `verify throws VALIDITY_UNTIL_OUT_OF_RANGE when validUntil is after cert notAfter`() {
        val info = validityInfo().copy(validUntil = validityPeriod.notAfter + 1.minutes)
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verify(validityPeriod, info)
        }
        assertThat(exception, hasError(VerificationError.VALIDITY_UNTIL_OUT_OF_RANGE))
    }
}
