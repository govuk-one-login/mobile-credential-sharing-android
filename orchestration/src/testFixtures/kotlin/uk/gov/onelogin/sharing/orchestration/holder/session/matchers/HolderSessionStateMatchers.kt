package uk.gov.onelogin.sharing.orchestration.holder.session.matchers

import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.v2.MissingPrerequisitesV2Matchers.hasPrerequisite
import uk.gov.onelogin.sharing.orchestration.session.SessionError

/**
 * Wrapper object for storing hamcrest [Matcher] functions for [HolderSessionState].
 */
object HolderSessionStateMatchers {
    fun inPreflight(): Matcher<in HolderSessionState> = instanceOf(
        HolderSessionState.Preflight::class.java
    )

    fun hasMissingPreflightPrerequisites(
        vararg prerequisite: Prerequisite
    ): Matcher<HolderSessionState> = hasMissingPreflightPrerequisites(
        prerequisite
            .map(::hasPrerequisite)
            .map(Matchers::contains)
            .let(::allOf)
    )

    fun hasMissingPreflightPrerequisites(
        matcher: Matcher<in List<MissingPrerequisiteV2>>
    ): Matcher<HolderSessionState> = HasHolderPreflightPrerequisites(matcher)

    fun isCancelled(): Matcher<in HolderSessionState> = equalTo(
        HolderSessionState.Complete.Cancelled
    )

    fun isFailed(message: String, exception: Exception): Matcher<in HolderSessionState> = equalTo(
        HolderSessionState.Complete.Failed(
            SessionError(message, exception)
        )
    )

    fun isFailed(matcher: Matcher<in SessionError>): Matcher<HolderSessionState> = IsFailed(matcher)

    fun isNotStarted(): Matcher<in HolderSessionState> = equalTo(
        HolderSessionState.NotStarted
    )

    fun isReadyToPresent(): Matcher<in HolderSessionState> = instanceOf(
        HolderSessionState.ReadyToPresent::class.java
    )

    fun inPresentingEngagement(): Matcher<in HolderSessionState> = instanceOf(
        HolderSessionState.PresentingEngagement::class.java
    )

    fun isProcessingEstablishment(): Matcher<in HolderSessionState> = instanceOf(
        HolderSessionState.ProcessingEstablishment::class.java
    )

    fun isAwaitingUserConsent(): Matcher<in HolderSessionState> = instanceOf(
        HolderSessionState.AwaitingUserConsent::class.java
    )
}
