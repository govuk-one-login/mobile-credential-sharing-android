package uk.gov.onelogin.sharing.cryptoService.util

import org.junit.Assert.assertTrue
import uk.gov.logging.testdouble.v2.LogEntry
import uk.gov.logging.testdouble.v2.SystemLogger

/**
 * Asserts that an error entry was logged carrying a throwable of [throwableType]
 * whose message satisfies [messagePredicate].
 */
fun SystemLogger.assertErrorLoggedWithThrowable(
    throwableType: Class<out Throwable>,
    messagePredicate: (String) -> Boolean
) {
    assertTrue(
        "Expected an error log with a ${throwableType.simpleName} throwable",
        any { entry ->
            entry is LogEntry.ErrorNoKeys &&
                throwableType.isInstance(entry.throwable) &&
                messagePredicate(entry.message)
        }
    )
}
