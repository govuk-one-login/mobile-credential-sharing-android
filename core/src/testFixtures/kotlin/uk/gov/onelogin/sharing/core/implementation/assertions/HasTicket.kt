package uk.gov.onelogin.sharing.core.implementation.assertions

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail

internal class HasTicket(private val matcher: Matcher<String>) :
    TypeSafeMatcher<ImplementationDetail>() {
    override fun matchesSafely(item: ImplementationDetail?): Boolean = matcher.matches(item?.ticket)

    override fun describeTo(description: Description?) {
        matcher.describeTo(description)
    }

    override fun describeMismatchSafely(
        item: ImplementationDetail?,
        mismatchDescription: Description?
    ) {
        matcher.describeMismatch(item?.ticket, mismatchDescription)
    }
}
