package uk.gov.onelogin.sharing.orchestration.holder.session.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2

internal class HasHolderPreflightPrerequisites(
    private val matcher: Matcher<in List<MissingPrerequisiteV2>>
) : TypeSafeMatcher<HolderSessionState>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)

    override fun describeMismatchSafely(
        item: HolderSessionState?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(
        (item as? HolderSessionState.Preflight)?.missingPrerequisites,
        mismatchDescription
    )

    override fun matchesSafely(item: HolderSessionState?): Boolean = matcher.matches(
        (item as? HolderSessionState.Preflight)?.missingPrerequisites
    )
}
