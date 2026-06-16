package uk.gov.onelogin.sharing.prerequisites

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object RecoverableMatchers {
    fun isRecoverable(expected: Boolean = true): Matcher<in Recoverable> =
        RecoverableMatcher(equalTo(expected))

    fun isUnrecoverable() = isRecoverable(false)

    private class RecoverableMatcher(private val matcher: Matcher<in Boolean>) :
        TypeSafeMatcher<Recoverable>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(item: Recoverable?, mismatchDescription: Description?) =
            matcher.describeMismatch(item?.isRecoverable(), mismatchDescription)

        override fun matchesSafely(item: Recoverable?): Boolean = matcher.matches(
            item?.isRecoverable()
        )
    }
}
