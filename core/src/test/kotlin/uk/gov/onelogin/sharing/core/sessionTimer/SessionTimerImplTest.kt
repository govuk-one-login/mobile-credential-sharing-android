package uk.gov.onelogin.sharing.core.sessionTimer

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger

@OptIn(ExperimentalCoroutinesApi::class)
class SessionTimerImplTest {
    private val logger = SystemLogger()

    @Test
    fun `timer executes action after delay`() = runTest {
        val timer = SessionTimerImpl(this, logger)
        var executed = false
        timer.start(10.seconds) {
            executed = true
        }

        advanceTimeBy(9.seconds)
        assertFalse(executed)

        advanceTimeBy(2.seconds)
        assertTrue(executed)
    }

    @Test
    fun `reset restarts the timer`() = runTest {
        val timer = SessionTimerImpl(this, logger)
        var executionCount = 0
        timer.start(10.seconds) {
            executionCount++
        }

        advanceTimeBy(5.seconds)
        timer.reset()

        advanceTimeBy(6.seconds)

        assertEquals(0, executionCount)

        advanceTimeBy(5.seconds)
        assertEquals(1, executionCount)
    }

    @Test
    fun `stop cancels the timer`() = runTest {
        val timer = SessionTimerImpl(this, logger)
        var executed = false
        timer.start(10.seconds) {
            executed = true
        }

        advanceTimeBy(5.seconds)
        timer.stop()

        advanceTimeBy(10.seconds)
        assertFalse(executed)
    }

    @Test
    fun `start after stop works correctly`() = runTest {
        val timer = SessionTimerImpl(this, logger)
        var executionCount = 0
        timer.start(10.seconds) { executionCount++ }
        timer.stop()

        timer.start(5.seconds) { executionCount++ }
        advanceTimeBy(6.seconds)

        assertEquals(1, executionCount)
    }

    @Test
    fun `reset before start does nothing`() = runTest {
        val timer = SessionTimerImpl(this, logger)
        timer.reset()
    }

    @Test
    fun `stop before start does nothing`() = runTest {
        val timer = SessionTimerImpl(this, logger)
        timer.stop()
    }
}
