package uk.gov.onelogin.sharing.orchestration.prerequisites.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.readiness.NotReadyReason

internal class HasNotReadyReason(private val matcher: Matcher<in NotReadyReason>) :
    TypeSafeMatcher<MissingPrerequisiteReason>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)

    override fun describeMismatchSafely(
        item: MissingPrerequisiteReason?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(
        (item as? MissingPrerequisiteReason.NotReady)?.reason,
        mismatchDescription
    )

    override fun matchesSafely(item: MissingPrerequisiteReason?): Boolean =
        matcher.matches((item as? MissingPrerequisiteReason.NotReady)?.reason)
}
