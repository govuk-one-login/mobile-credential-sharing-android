package uk.gov.onelogin.sharing.orchestration.prerequisites.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteReason

internal data class HasMissingPrerequisiteReason(
    private val matcher: Matcher<in MissingPrerequisiteReason>
) : TypeSafeMatcher<MissingPrerequisite>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)
    override fun describeMismatchSafely(
        item: MissingPrerequisite?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(item?.reason, mismatchDescription)

    override fun matchesSafely(item: MissingPrerequisite?): Boolean = matcher.matches(item?.reason)
}
