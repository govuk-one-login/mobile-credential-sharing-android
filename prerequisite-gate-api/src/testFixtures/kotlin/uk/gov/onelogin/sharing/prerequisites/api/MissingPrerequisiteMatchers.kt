package uk.gov.onelogin.sharing.prerequisites

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.equalTo
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.prerequisites.api.MissingPrerequisite
import uk.gov.onelogin.sharing.prerequisites.api.Prerequisite

object MissingPrerequisiteMatchers {
    fun hasPrerequisite(expected: Prerequisite): Matcher<in MissingPrerequisite> =
        MissingPrerequisiteMatcher(equalTo(expected)) { it?.prerequisite }

    private class MissingPrerequisiteMatcher<Type>(
        private val matcher: Matcher<in Type>,
        private val transformer: (MissingPrerequisite?) -> Type?
    ) : TypeSafeMatcher<MissingPrerequisite>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: MissingPrerequisite?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)

        override fun matchesSafely(item: MissingPrerequisite?): Boolean = matcher.matches(
            transformer(item)
        )
    }
}
