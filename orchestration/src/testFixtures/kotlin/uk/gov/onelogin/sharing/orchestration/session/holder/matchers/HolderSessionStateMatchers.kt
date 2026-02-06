package uk.gov.onelogin.sharing.orchestration.session.holder.matchers

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState

/**
 * Wrapper object for storing hamcrest [Matcher] functions for [HolderSessionState].
 */
object HolderSessionStateMatchers {
    fun inPreflight(): Matcher<HolderSessionState> = instanceOf(
        HolderSessionState.Preflight::class.java
    )

    fun isCancelled(): Matcher<HolderSessionState> = equalTo(HolderSessionState.Complete.Cancelled)
}
