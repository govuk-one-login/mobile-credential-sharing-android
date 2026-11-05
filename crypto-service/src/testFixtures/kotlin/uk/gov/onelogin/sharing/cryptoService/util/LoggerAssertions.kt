package uk.gov.onelogin.sharing.cryptoService.util

import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import uk.gov.logging.api.v3.LogEntry
import uk.gov.logging.api.v3.MemorisedLogger
import uk.gov.logging.api.v3.matchers.LogEntryMatchers.hasException
import uk.gov.logging.api.v3.matchers.LogEntryMatchers.hasMessage

fun MemorisedLogger.assertErrorLoggedWithThrowable(
    throwableType: Class<out Throwable>,
    messageMatcher: Matcher<String>
) {
    assertThat(
        "Expected an error log with a ${throwableType.simpleName} throwable",
        this,
        Matchers.contains(
            allOf(
                instanceOf(LogEntry.Error::class.java),
                hasException(instanceOf(throwableType)),
                hasMessage(messageMatcher)
            )
        )
    )
}
