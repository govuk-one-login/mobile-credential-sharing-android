package uk.gov.onelogin.sharing.verification.models

import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.result.VerificationError
import uk.gov.onelogin.sharing.verification.result.VerificationResult
import uk.gov.onelogin.sharing.verification.result.VerificationResultMatchers.hasError

@OptIn(ExperimentalTime::class)
class MobileSecurityObjectTest {

    private val now = Clock.System.now()

    /**
     * DCMAW-20245: AC11: [DeviceKeyInfo] and [ValidityInfo] can be constructed independently and
     * embedded within MobileSecurityObject.
     */
    private val mso = MobileSecurityObject(
        docType = "Unit test",
        valueDigests = emptyMap(),
        deviceKeyInfo = DeviceKeyInfo(
            deviceKey = byteArrayOf(),
        ),
        validityInfo = ValidityInfo(
            signed = now,
            validFrom = now.minus(2.minutes),
            validUntil = now.plus(2.minutes)
        )
    )

    /**
     * DCMAW-20245: AC10: [MobileSecurityObject] can be constructed with all required fields and
     * correctly implements value equality (including byte array fields).
     */
    @Test
    fun `Equality is based on value, not reference`() {
        assertEquals(mso, mso.copy())
        listOf(
            mso.copy(docType = "Another"),
            mso.copy(valueDigests = mapOf("Test" to emptyMap())),
            mso.copy(deviceKeyInfo = DeviceKeyInfo(deviceKey = byteArrayOf(1, 2))),
            mso.copy(
                validityInfo = mso.validityInfo.copy(
                    signed = now.minus(1.minutes)
                )
            ),
            mso.copy(status = byteArrayOf(2, 3))
        ).forEach {
            assertNotEquals(mso, it)
        }
    }

    @Test
    fun `Invalid versions throw Verification failures`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            mso.copy(version = "2.0")
        }

        assertThat(
            exception,
            hasError(VerificationError.INVALID_MSO_VERSION)
        )
    }

    @Test
    fun `Invalid digest algorithms throw Verification failures`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            mso.copy(digestAlgorithm = "NotAnAlgorithm")
        }

        assertThat(
            exception,
            hasError(VerificationError.UNSUPPORTED_DIGEST_ALGORITHM)
        )
    }

}