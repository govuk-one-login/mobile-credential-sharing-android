package uk.gov.onelogin.sharing.orchestration.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.orchestration.VerifierOrchestrator
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSession

class HasVerifierSession(private val matcher: Matcher<in VerifierSession>) :
    TypeSafeMatcher<VerifierOrchestrator>() {
    override fun describeTo(description: Description?) {
        matcher.describeTo(description)
    }

    override fun describeMismatchSafely(
        item: VerifierOrchestrator?,
        mismatchDescription: Description?
    ) {
        matcher.describeMismatch(item?.session, mismatchDescription)
    }

    override fun matchesSafely(item: VerifierOrchestrator?): Boolean =
        matcher.matches(item?.session)
}
