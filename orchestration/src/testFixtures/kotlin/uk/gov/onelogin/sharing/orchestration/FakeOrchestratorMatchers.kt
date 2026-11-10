package uk.gov.onelogin.sharing.orchestration

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.equalTo
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

object FakeOrchestratorMatchers {
    fun hasCancelCount(expected: Int) = hasCancelCount(equalTo(expected))

    fun hasCancelCount(matcher: Matcher<in Int>): Matcher<in FakeOrchestrator> =
        FakeOrchestratorMatcher(matcher) {
            it?.cancelCount
        }

    fun hasDenyConsentCount(expected: Int) = hasDenyConsentCount(equalTo(expected))

    fun hasDenyConsentCount(matcher: Matcher<in Int>): Matcher<in FakeOrchestrator> =
        FakeOrchestratorMatcher(matcher) {
            it?.denyConsentCount
        }

    fun hasHolderSessionState(expected: HolderSessionState) =
        hasHolderSessionState(equalTo(expected))

    fun hasHolderSessionState(
        matcher: Matcher<in HolderSessionState>
    ): Matcher<in FakeOrchestrator> = FakeOrchestratorMatcher(matcher) {
        it?.holderSessionState?.value
    }

    fun hasResetCount(expected: Int) = hasResetCount(equalTo(expected))

    fun hasResetCount(matcher: Matcher<in Int>): Matcher<in FakeOrchestrator> =
        FakeOrchestratorMatcher(matcher) {
            it?.resetCount
        }

    fun hasStartCount(expected: Int) = hasStartCount(equalTo(expected))

    fun hasStartCount(matcher: Matcher<in Int>): Matcher<in FakeOrchestrator> =
        FakeOrchestratorMatcher(matcher) {
            it?.startCount
        }

    fun hasVerifierSessionState(expected: HolderSessionState) =
        hasVerifierSessionState(equalTo(expected))

    fun hasVerifierSessionState(
        matcher: Matcher<in VerifierSessionState>
    ): Matcher<in FakeOrchestrator> = FakeOrchestratorMatcher(matcher) {
        it?.verifierSessionState?.value
    }

    private class FakeOrchestratorMatcher<T>(
        private val matcher: Matcher<in T>,
        private val transformer: (FakeOrchestrator?) -> T?
    ) : TypeSafeMatcher<FakeOrchestrator>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: FakeOrchestrator?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)

        override fun matchesSafely(item: FakeOrchestrator?): Boolean =
            matcher.matches(transformer(item))
    }
}
