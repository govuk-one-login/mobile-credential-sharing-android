package uk.gov.onelogin.sharing.prerequisites

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object ActionablePrerequisiteMatchers {
    fun hasAction(
        matcher: Matcher<in PrerequisiteAction>
    ): Matcher<in Actionable<PrerequisiteAction>> = ActionablePrerequisiteMatcher(matcher)

    private class ActionablePrerequisiteMatcher(
        private val matcher: Matcher<in PrerequisiteAction>
    ) : TypeSafeMatcher<Actionable<PrerequisiteAction>>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: Actionable<PrerequisiteAction>?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(item?.getAction(), mismatchDescription)

        override fun matchesSafely(
            item: Actionable<PrerequisiteAction>?
        ): Boolean = matcher.matches(item?.getAction())
    }
}