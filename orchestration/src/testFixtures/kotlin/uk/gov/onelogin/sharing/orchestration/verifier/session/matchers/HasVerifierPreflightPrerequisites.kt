package uk.gov.onelogin.sharing.orchestration.verifier.session.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

internal class HasVerifierPreflightPrerequisites(
    private val matcher: Matcher<in List<MissingPrerequisiteV2>>
) : TypeSafeMatcher<VerifierSessionState>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)

    override fun describeMismatchSafely(
        item: VerifierSessionState?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(
        (item as? VerifierSessionState.Preflight)?.missingPrerequisites,
        mismatchDescription
    )

    override fun matchesSafely(item: VerifierSessionState?): Boolean = matcher.matches(
        (item as? VerifierSessionState.Preflight)?.missingPrerequisites
    )
}
