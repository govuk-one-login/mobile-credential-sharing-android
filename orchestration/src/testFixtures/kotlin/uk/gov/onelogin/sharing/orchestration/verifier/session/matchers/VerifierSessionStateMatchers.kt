package uk.gov.onelogin.sharing.orchestration.verifier.session.matchers

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

/**
 * Wrapper object for storing hamcrest [Matcher] functions for [VerifierSessionState].
 */
object VerifierSessionStateMatchers {
    fun inPreflight(): Matcher<VerifierSessionState> = instanceOf(
        VerifierSessionState.Preflight::class.java
    )

    fun isCancelled(): Matcher<VerifierSessionState> = equalTo(
        VerifierSessionState.Complete.Cancelled
    )

    fun isNotStarted(): Matcher<VerifierSessionState> = equalTo(
        VerifierSessionState.NotStarted
    )
}
