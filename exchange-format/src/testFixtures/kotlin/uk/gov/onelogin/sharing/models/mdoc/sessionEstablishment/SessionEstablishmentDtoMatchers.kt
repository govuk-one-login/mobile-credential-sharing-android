package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor

object SessionEstablishmentDtoMatchers {
    fun hasData(expected: ByteArray) = hasData(equalTo(expected))

    fun hasData(matcher: Matcher<in ByteArray>): Matcher<in SessionEstablishmentDto> =
        SessionEstablishmentDtoMatcher(matcher) {
            it?.data
        }

    fun hasEReaderKey(expected: EmbeddedCbor) = hasEReaderKey(equalTo(expected))

    fun hasEReaderKey(matcher: Matcher<in EmbeddedCbor>): Matcher<in SessionEstablishmentDto> =
        SessionEstablishmentDtoMatcher(matcher) {
            it?.eReaderKey
        }

    private class SessionEstablishmentDtoMatcher<Type>(
        private val matcher: Matcher<in Type>,
        private val transformer: (SessionEstablishmentDto?) -> Type?
    ) : TypeSafeMatcher<SessionEstablishmentDto>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: SessionEstablishmentDto?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)

        override fun matchesSafely(item: SessionEstablishmentDto?): Boolean = matcher.matches(
            transformer(item)
        )
    }
}
