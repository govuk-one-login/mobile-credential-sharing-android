package uk.gov.onelogin.sharing.orchestration.verifier.session.matchers

import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.prerequisites.MissingPrerequisiteMatchers.hasPrerequisite
import uk.gov.onelogin.sharing.prerequisites.api.MissingPrerequisite
import uk.gov.onelogin.sharing.prerequisites.api.Prerequisite
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument

/**
 * Wrapper object for storing hamcrest [Matcher] functions for [VerifierSessionState].
 */
object VerifierSessionStateMatchers {
    fun inPreflight(): Matcher<in VerifierSessionState> = instanceOf(
        VerifierSessionState.Preflight::class.java
    )

    fun hasMissingPreflightPrerequisites(
        vararg prerequisite: Prerequisite
    ): Matcher<in VerifierSessionState> = hasMissingPreflightPrerequisites(
        prerequisite
            .map(::hasPrerequisite)
            .map(Matchers::contains)
            .let(::allOf)
    )

    fun hasMissingPreflightPrerequisites(
        matcher: Matcher<in List<MissingPrerequisite>>
    ): Matcher<VerifierSessionState> = VerifierSessionStateMatcher(matcher) {
        (it as? VerifierSessionState.Preflight)?.missingPrerequisites
    }

    fun isCancelled(): Matcher<in VerifierSessionState> = equalTo(
        VerifierSessionState.Complete.Cancelled
    )

    fun isNotStarted(): Matcher<in VerifierSessionState> = equalTo(
        VerifierSessionState.NotStarted
    )

    fun isReadyToScan(): Matcher<in VerifierSessionState> = equalTo(
        VerifierSessionState.ReadyToScan
    )

    fun isProcessingEngagement(): Matcher<in VerifierSessionState> = equalTo(
        VerifierSessionState.ProcessingEngagement
    )

    fun isConnecting(): Matcher<in VerifierSessionState> = equalTo(
        VerifierSessionState.Connecting
    )

    fun isFailed(): Matcher<in VerifierSessionState> = instanceOf(
        VerifierSessionState.Complete.Failed::class.java
    )

    fun isFailed(matcher: Matcher<in SessionError>): Matcher<in VerifierSessionState> =
        VerifierSessionStateMatcher(matcher) {
            (it as? VerifierSessionState.Complete.Failed)?.error
        }

    fun isSuccess(
        matcher: Matcher<in VerifierSessionState.Complete.Success> = instanceOf(
            VerifierSessionState.Complete.Success::class.java
        )
    ): Matcher<in VerifierSessionState> = VerifierSessionStateMatcher(matcher) {
        (it as? VerifierSessionState.Complete.Success)
    }

    object SuccessMatchers {
        fun hasDocumentCount(expected: Int) = hasDocumentCount(equalTo(expected))

        fun hasDocumentCount(
            matcher: Matcher<in Int>
        ): Matcher<in VerifierSessionState.Complete.Success> = VerifierSessionStateMatcher(
            matcher
        ) {
            (it as? VerifierSessionState.Complete.Success)?.size
        }

        fun hasDocuments(expected: Iterable<VerifiableDocument.WithPresentation>) =
            hasDocuments(equalTo(expected))

        fun hasDocuments(
            matcher: Matcher<in Iterable<VerifiableDocument.WithPresentation>>
        ): Matcher<in VerifierSessionState.Complete.Success> = VerifierSessionStateMatcher(
            matcher
        ) {
            (it as? VerifierSessionState.Complete.Success)
        }
    }

    private class VerifierSessionStateMatcher<Type>(
        private val matcher: Matcher<in Type>,
        private val transformer: (VerifierSessionState?) -> Type?
    ) : TypeSafeMatcher<VerifierSessionState>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: VerifierSessionState?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)
        override fun matchesSafely(item: VerifierSessionState?): Boolean = matcher.matches(
            transformer(item)
        )
    }
}
