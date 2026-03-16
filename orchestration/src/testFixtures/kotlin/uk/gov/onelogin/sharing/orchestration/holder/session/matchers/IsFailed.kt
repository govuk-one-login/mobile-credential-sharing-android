package uk.gov.onelogin.sharing.orchestration.holder.session.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionError

internal class IsFailed(private val matcher: Matcher<in SessionError>) :
    TypeSafeMatcher<HolderSessionState>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)

    override fun describeMismatchSafely(
        item: HolderSessionState?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(
        (item as? HolderSessionState.Complete.Failed)?.error,
        mismatchDescription
    )

    override fun matchesSafely(item: HolderSessionState?): Boolean = matcher.matches(
        (item as? HolderSessionState.Complete.Failed)?.error
    )
}
