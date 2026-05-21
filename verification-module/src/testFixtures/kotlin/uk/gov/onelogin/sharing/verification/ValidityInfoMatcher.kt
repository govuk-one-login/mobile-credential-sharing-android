package uk.gov.onelogin.sharing.verification

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

internal class ValidityInfoMatcher<Type>(
    private val matcher: Matcher<in Type>,
    private val transformer: (ValidityInfo?) -> Type?
) : TypeSafeMatcher<ValidityInfo>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)
    override fun describeMismatchSafely(
        item: ValidityInfo?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(transformer(item), mismatchDescription)

    override fun matchesSafely(item: ValidityInfo?): Boolean =
        matcher.matches(transformer(item))
}
