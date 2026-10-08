package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object ItemsRequestDtoMatchers {
    fun hasElementCount(expected: Int) = hasElementCount(equalTo(expected))

    fun hasElementCount(matcher: Matcher<Int>): Matcher<in ItemsRequestDto> =
        ItemsRequestDtoMatcher(matcher) { it?.elementCount }

    private class ItemsRequestDtoMatcher<Type>(
        private val matcher: Matcher<in Type>,
        private val transformer: (ItemsRequestDto?) -> Type?
    ) : TypeSafeMatcher<ItemsRequestDto>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)

        override fun describeMismatchSafely(
            item: ItemsRequestDto?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)

        override fun matchesSafely(item: ItemsRequestDto?): Boolean = matcher.matches(
            transformer(item)
        )
    }
}
