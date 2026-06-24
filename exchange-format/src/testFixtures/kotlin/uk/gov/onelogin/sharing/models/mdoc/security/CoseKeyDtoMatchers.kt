package uk.gov.onelogin.sharing.models.mdoc.security

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object CoseKeyDtoMatchers {
    fun hasCurveType(expected: Long) = hasCurveType(equalTo(expected))

    fun hasCurveType(matcher: Matcher<in Long>): Matcher<in CoseKeyDto> =
        CoseKeyDtoMatcher(matcher) {
            it?.curve
        }

    fun hasKeyType(expected: Long) = hasKeyType(equalTo(expected))

    fun hasKeyType(matcher: Matcher<in Long>): Matcher<in CoseKeyDto> = CoseKeyDtoMatcher(matcher) {
        it?.keyType
    }

    fun hasX(expected: ByteArray) = hasX(equalTo(expected))

    fun hasX(matcher: Matcher<in ByteArray>): Matcher<in CoseKeyDto> = CoseKeyDtoMatcher(matcher) {
        it?.x
    }

    fun hasY(expected: ByteArray) = hasY(equalTo(expected))

    fun hasY(matcher: Matcher<in ByteArray>): Matcher<in CoseKeyDto> = CoseKeyDtoMatcher(matcher) {
        it?.y
    }

    private class CoseKeyDtoMatcher<Type>(
        private val matcher: Matcher<in Type>,
        private val transformer: (CoseKeyDto?) -> Type?
    ) : TypeSafeMatcher<CoseKeyDto>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(item: CoseKeyDto?, mismatchDescription: Description?) =
            matcher.describeMismatch(transformer(item), mismatchDescription)

        override fun matchesSafely(item: CoseKeyDto?): Boolean = matcher.matches(
            transformer(item)
        )
    }
}
