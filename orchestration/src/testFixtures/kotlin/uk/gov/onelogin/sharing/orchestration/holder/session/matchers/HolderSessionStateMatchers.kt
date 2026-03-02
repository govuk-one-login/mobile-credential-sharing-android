package uk.gov.onelogin.sharing.orchestration.holder.session.matchers

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

/**
 * Wrapper object for storing hamcrest [Matcher] functions for [HolderSessionState].
 */
object HolderSessionStateMatchers {
    fun inPreflight(): Matcher<in HolderSessionState> = instanceOf(
        HolderSessionState.Preflight::class.java
    )

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
