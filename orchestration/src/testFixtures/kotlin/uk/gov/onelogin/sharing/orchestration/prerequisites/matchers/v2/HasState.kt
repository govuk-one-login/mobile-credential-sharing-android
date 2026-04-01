package uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.v2

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2

internal open class HasState<T : Any>(
    private val matcher: Matcher<in T>,
    private val extract: (MissingPrerequisiteV2) -> T?
) : TypeSafeMatcher<MissingPrerequisiteV2>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)
    override fun describeMismatchSafely(
        item: MissingPrerequisiteV2?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(item?.let(extract), mismatchDescription)

    override fun matchesSafely(item: MissingPrerequisiteV2?): Boolean =
        matcher.matches(item?.let(extract))
}
