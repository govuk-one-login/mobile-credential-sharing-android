package uk.gov.onelogin.sharing.orchestration.prerequisites.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite

internal class HasPrerequisite(private val matcher: Matcher<in Prerequisite>) :
    TypeSafeMatcher<MissingPrerequisite>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)
    override fun describeMismatchSafely(
        item: MissingPrerequisite?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(item?.prerequisite, mismatchDescription)

    override fun matchesSafely(item: MissingPrerequisite?): Boolean =
        matcher.matches(item?.prerequisite)
}
