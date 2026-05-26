package uk.gov.onelogin.sharing.verification.format.document

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object IssuerSignedMatchers {
    fun hasIssuerAuth(expected: ByteArray) = hasIssuerAuth(equalTo(expected))
    fun hasIssuerAuth(matcher: Matcher<in ByteArray>): Matcher<in IssuerSigned> =
        IssuerSignedMatcher(matcher) {
            it?.issuerAuth
        }
    fun hasNameSpaces(expected: Map<String, ByteArray>) = hasNameSpaces(equalTo(expected))
    fun hasNameSpaces(matcher: Matcher<in Map<String, ByteArray>>): Matcher<in IssuerSigned> =
        IssuerSignedMatcher(matcher) {
            it?.nameSpaces
        }

    private class IssuerSignedMatcher<Type>(
        private val matcher: Matcher<in Type>,
        private val transformer: (IssuerSigned?) -> Type?
    ) : TypeSafeMatcher<IssuerSigned>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: IssuerSigned?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)
        override fun matchesSafely(item: IssuerSigned?): Boolean =
            matcher.matches(transformer(item))
    }
}
