package uk.gov.onelogin.sharing.orchestration.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.orchestration.HolderOrchestrator
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSession

class HasHolderSession(
    private val matcher: Matcher<in HolderSession>
) : TypeSafeMatcher<HolderOrchestrator>() {
    override fun describeTo(description: Description?) {
        matcher.describeTo(description)
    }

    override fun describeMismatchSafely(
        item: HolderOrchestrator?,
        mismatchDescription: Description?
    ) {
        matcher.describeMismatch(item?.session, mismatchDescription)
    }

    override fun matchesSafely(
        item: HolderOrchestrator?
    ): Boolean = matcher.matches(item?.session)
}