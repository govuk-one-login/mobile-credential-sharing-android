package uk.gov.onelogin.sharing.orchestration.verifier.session.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

internal class IsFailed(private val matcher: Matcher<in SessionError>) :
    TypeSafeMatcher<VerifierSessionState>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)

    override fun describeMismatchSafely(
        item: VerifierSessionState?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(
        (item as? VerifierSessionState.Complete.Failed)?.error,
        mismatchDescription
    )

    override fun matchesSafely(item: VerifierSessionState?): Boolean = matcher.matches(
        (item as? VerifierSessionState.Complete.Failed)?.error
    )
}
