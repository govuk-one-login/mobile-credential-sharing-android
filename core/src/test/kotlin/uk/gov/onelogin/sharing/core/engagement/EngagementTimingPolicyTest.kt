package uk.gov.onelogin.sharing.core.engagement

import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class EngagementTimingPolicyTest {

    @Test
    fun `qrVisibleDuration is always exactly graceDelay shorter than the deadline`() {
        val policy = EngagementTimingPolicy(deadline = 300.seconds, graceDelay = 1.seconds)

        assertEquals(299.seconds, policy.qrVisibleDuration)
        assertEquals(policy.deadline - policy.graceDelay, policy.qrVisibleDuration)
    }

    @Test
    fun `defaults are 300s deadline with a 1s grace delay`() {
        val policy = EngagementTimingPolicy()

        assertEquals(300.seconds, policy.deadline)
        assertEquals(1.seconds, policy.graceDelay)
        assertEquals(299.seconds, policy.qrVisibleDuration)
    }

    @Test
    fun `reconfiguring only the deadline keeps the 1s grace relationship`() {
        val policy = EngagementTimingPolicy(deadline = 10.seconds)

        assertEquals(1.seconds, policy.graceDelay)
        assertEquals(9.seconds, policy.qrVisibleDuration)
    }

    @Test
    fun `grace delay not shorter than the deadline is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            EngagementTimingPolicy(deadline = 1.seconds, graceDelay = 1.seconds)
        }
    }
}
