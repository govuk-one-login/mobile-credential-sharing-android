package uk.gov.onelogin.sharing.core.coroutines

import kotlinx.coroutines.Job
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object JobMatchers {
    fun isActive(expected: Boolean = true) = isActive(equalTo(expected))

    fun isActive(matcher: Matcher<in Boolean>): Matcher<in Job> = JobMatcher(matcher) {
        it?.isActive
    }

    fun isCancelled(expected: Boolean = true) = isCancelled(equalTo(expected))

    fun isCancelled(matcher: Matcher<in Boolean>): Matcher<in Job> = JobMatcher(matcher) {
        it?.isCancelled
    }

    private class JobMatcher<Type>(
        private val matcher: Matcher<in Type>,
        private val transformer: (Job?) -> Type?
    ) : TypeSafeMatcher<Job>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(item: Job?, mismatchDescription: Description?) =
            matcher.describeMismatch(transformer(item), mismatchDescription)

        override fun matchesSafely(item: Job?): Boolean = matcher.matches(transformer(item))
    }
}
