package uk.gov.onelogin.sharing.testapp.logger

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import uk.gov.logging.api.v2.errorKeys.ErrorKeys

class SystemCrashLoggerTest {
    private val logger = SystemCrashLogger()
    private val output = ByteArrayOutputStream()
    private lateinit var originalOut: PrintStream

    @Before
    fun setUp() {
        originalOut = System.out
        System.setOut(PrintStream(output))
    }

    @After
    fun tearDown() {
        System.setOut(originalOut)
    }

    @Test
    fun `log with throwable and error keys prints message with keys`() {
        logger.log(
            RuntimeException("test"),
            ErrorKeys.StringKey("key1", "value1")
        )

        assertEquals("Crash logged: test [key1=value1]", output.toString().trim())
    }
}
