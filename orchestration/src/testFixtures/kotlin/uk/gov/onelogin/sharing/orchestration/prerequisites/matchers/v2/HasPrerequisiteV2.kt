package uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.v2

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite

internal class HasPrerequisiteV2(private val matcher: Matcher<in Prerequisite>) :
    TypeSafeMatcher<MissingPrerequisiteV2>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)
    override fun describeMismatchSafely(
        item: MissingPrerequisiteV2?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(item?.prerequisite, mismatchDescription)

    override fun matchesSafely(item: MissingPrerequisiteV2?): Boolean =
        matcher.matches(item?.prerequisite)
}
