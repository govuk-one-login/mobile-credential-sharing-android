package uk.gov.onelogin.sharing.verification.format.document

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceSigned

object VerifiableDocumentMatchers {
    fun hasDocType(expected: String) = hasDocType(equalTo(expected))

    fun hasDocType(matcher: Matcher<in String>): Matcher<in VerifiableDocument> =
        VerifiableDocumentMatcher(matcher) {
            it?.docType
        }

    fun hasIssuerSigned(expected: IssuerSigned) = hasIssuerSigned(equalTo(expected))

    fun hasIssuerSigned(matcher: Matcher<in IssuerSigned>): Matcher<in VerifiableDocument> =
        VerifiableDocumentMatcher(matcher) {
            it?.issuerSigned
        }

    fun hasDeviceSigned(expected: DeviceSigned) = hasDeviceSigned(equalTo(expected))

    fun hasDeviceSigned(
        matcher: Matcher<in DeviceSigned>
    ): Matcher<in VerifiableDocument.WithPresentation> = VerifiableDocumentWithPresentationMatcher(
        matcher
    ) { it?.deviceSigned }

    private class VerifiableDocumentMatcher<Type>(
        private val matcher: Matcher<Type>,
        private val transformer: (VerifiableDocument?) -> Type?
    ) : TypeSafeMatcher<VerifiableDocument>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: VerifiableDocument?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)
        override fun matchesSafely(item: VerifiableDocument?): Boolean = matcher.matches(
            transformer(item)
        )
    }

    private class VerifiableDocumentWithPresentationMatcher<Type>(
        private val matcher: Matcher<Type>,
        private val transformer: (VerifiableDocument.WithPresentation?) -> Type?
    ) : TypeSafeMatcher<VerifiableDocument.WithPresentation>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: VerifiableDocument.WithPresentation?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)
        override fun matchesSafely(item: VerifiableDocument.WithPresentation?): Boolean =
            matcher.matches(transformer(item))
    }
}
