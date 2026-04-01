package uk.gov.onelogin.sharing.orchestration.verifier.session.matchers

import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.v2.MissingPrerequisitesV2Matchers.hasPrerequisite
import uk.gov.onelogin.sharing.orchestration.session.SessionError
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
            .map(::hasPrerequisite)
            .map(Matchers::contains)
            .let(::allOf)
    )

    fun hasMissingPreflightPrerequisites(
        matcher: Matcher<in List<MissingPrerequisiteV2>>
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

    fun isProcessingEngagement(): Matcher<VerifierSessionState> = equalTo(
        VerifierSessionState.ProcessingEngagement
    )

    fun isConnecting(): Matcher<VerifierSessionState> = equalTo(
        VerifierSessionState.Connecting
    )

    fun isFailed(): Matcher<VerifierSessionState> = instanceOf(
        VerifierSessionState.Complete.Failed::class.java
    )

    fun isFailed(matcher: Matcher<in SessionError>): Matcher<VerifierSessionState> =
        IsFailed(matcher)
}
