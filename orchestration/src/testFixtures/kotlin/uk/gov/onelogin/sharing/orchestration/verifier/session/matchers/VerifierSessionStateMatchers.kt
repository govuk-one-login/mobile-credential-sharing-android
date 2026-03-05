package uk.gov.onelogin.sharing.orchestration.verifier.session.matchers

import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Matcher
import org.hamcrest.Matchers.hasKey
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

/**
 * Wrapper object for storing hamcrest [Matcher] functions for [VerifierSessionState].
 */
object VerifierSessionStateMatchers {
    fun inPreflight(): Matcher<VerifierSessionState> = instanceOf(
        VerifierSessionState.Preflight::class.java
    )

    fun hasMissingPreflightPrerequisites(
        vararg prerequisite: Prerequisite
    ): Matcher<VerifierSessionState> = hasMissingPreflightPrerequisites(
        prerequisite
            .map(::hasKey)
            .let(::allOf)
    )

    fun hasMissingPreflightPrerequisites(
        matcher: Matcher<in Map<Prerequisite, PrerequisiteResponse>>
    ): Matcher<VerifierSessionState> = HasVerifierPreflightPrerequisites(matcher)

    fun isCancelled(): Matcher<VerifierSessionState> = equalTo(
        VerifierSessionState.Complete.Cancelled
    )

    fun isNotStarted(): Matcher<VerifierSessionState> = equalTo(
        VerifierSessionState.NotStarted
    )

    fun isReadyToScan(): Matcher<VerifierSessionState> = equalTo(
        VerifierSessionState.ReadyToScan
    )
}
