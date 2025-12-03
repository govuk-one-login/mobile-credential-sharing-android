package uk.gov.onelogin.sharing.core.logger

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger

class StandardLoggerFactoryTest {
    @Test
    fun `create returns SystemLogger instance`() {
        val logger = StandardLoggerFactory.create()

        assertTrue(logger is SystemLogger)
    }

    @Test
    fun `logTag works for an object instance`() {
        assertEquals("StandardLoggerFactory", StandardLoggerFactory.logTag)
    }
}
