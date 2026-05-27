package uk.gov.onelogin.sharing.verification.format.document.validity

import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import uk.gov.onelogin.sharing.verification.format.document.validity.ValidityInfoMatchers.hasExpectedUpdate

@OptIn(ExperimentalTime::class)
class ValidityInfoTest {
    private val now = Clock.System.now()

    private val info = ValidityInfo(
        signed = now.minus(1.minutes),
        validFrom = now.minus(30.seconds),
        validUntil = now.plus(1.minutes)
    )

    /**
     * DCMAW-20245: AC12: [ValidityInfo.expectedUpdate] is nullable and defaults to null when
     * absent.
     */
    @Test
    fun `Expected update property is null by default`() {
        assertThat(
            info,
            hasExpectedUpdate(nullValue())
        )
    }
}
