package uk.gov.onelogin.sharing.orchestration.holder.session.matchers

import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Matcher
import org.hamcrest.Matchers.hasKey
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse

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
            .map(::hasKey)
            .let(::allOf)
    )

    fun hasMissingPreflightPrerequisites(
        matcher: Matcher<in Map<Prerequisite, PrerequisiteResponse>>
    ): Matcher<HolderSessionState> = HasHolderPreflightPrerequisites(matcher)

    fun isCancelled(): Matcher<in HolderSessionState> = equalTo(
        HolderSessionState.Complete.Cancelled
    )

    fun isNotStarted(): Matcher<in HolderSessionState> = equalTo(
        HolderSessionState.NotStarted
    )

    fun inPresentingEngagement(): Matcher<in HolderSessionState> = instanceOf(
        HolderSessionState.PresentingEngagement::class.java
    )
}
