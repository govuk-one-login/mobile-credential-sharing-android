package uk.gov.onelogin.sharing.orchestration.session.matchers

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import uk.gov.onelogin.sharing.orchestration.session.matchers.SessionErrorReasonMatchers.UnverifiableDocumentMatchers.hasError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError

object SessionErrorReasonMatchers {

    fun isUnrecoverableThrowable(
        matcher: Matcher<in SessionErrorReason.UnrecoverableThrowable> = instanceOf(
            SessionErrorReason.UnrecoverableThrowable::class.java
        )
    ): Matcher<in SessionErrorReason> = SessionErrorReasonMatcher(matcher) {
        it as? SessionErrorReason.UnrecoverableThrowable
    }

    fun isUnrecoverablePrerequisite(
        matcher: Matcher<in SessionErrorReason.UnrecoverablePrerequisite> = instanceOf(
            SessionErrorReason.UnrecoverablePrerequisite::class.java
        )
    ): Matcher<in SessionErrorReason> = SessionErrorReasonMatcher(matcher) {
        it as? SessionErrorReason.UnrecoverablePrerequisite
    }

    fun isUnverifiableDocument(expected: SessionErrorReason.UnverifiableDocument) =
        isUnverifiableDocument(equalTo(expected))

    fun isUnverifiableDocument(failure: VerificationError) =
        isUnverifiableDocument(hasError(failure))

    fun isUnverifiableDocument(
        matcher: Matcher<in SessionErrorReason.UnverifiableDocument> = instanceOf(
            SessionErrorReason.UnverifiableDocument::class.java
        )
    ): Matcher<in SessionErrorReason> = SessionErrorReasonMatcher(matcher) {
        it as? SessionErrorReason.UnverifiableDocument
    }

    object UnrecoverableThrowableMatchers {
        fun hasSessionErrorThrowable(
            matcher: Matcher<in Throwable>
        ): Matcher<in SessionErrorReason.UnrecoverableThrowable> =
            SessionErrorReasonMatcher(matcher) {
                (it as? SessionErrorReason.UnrecoverableThrowable)?.exception
            }
    }

    object UnverifiableDocumentMatchers {
        fun hasError(expected: VerificationError) = hasError(equalTo(expected))

        fun hasError(
            matcher: Matcher<in VerificationError>
        ): Matcher<in SessionErrorReason.UnverifiableDocument> =
            SessionErrorReasonMatcher(matcher) {
                (it as? SessionErrorReason.UnverifiableDocument)?.error
            }
    }

    private class SessionErrorReasonMatcher<Type>(
        private val matcher: Matcher<in Type>,
        private val transformer: (SessionErrorReason?) -> Type?
    ) : TypeSafeMatcher<SessionErrorReason>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: SessionErrorReason?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)

        override fun matchesSafely(item: SessionErrorReason?): Boolean = matcher.matches(
            transformer(item)
        )
    }
}
