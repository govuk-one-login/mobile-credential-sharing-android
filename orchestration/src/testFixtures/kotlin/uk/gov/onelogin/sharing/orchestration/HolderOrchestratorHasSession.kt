package uk.gov.onelogin.sharing.orchestration

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.orchestration.HolderOrchestrator
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSession

/**
 * Custom [HolderOrchestrator] hamcrest [Matcher] that asserts against
 * [HolderOrchestrator.session].
 */
internal class HolderOrchestratorHasSession(private val matcher: Matcher<HolderSession>) :
    TypeSafeMatcher<HolderOrchestrator>() {
    override fun describeTo(description: Description?) {
        matcher.describeTo(description)
    }

    override fun describeMismatchSafely(
        item: HolderOrchestrator?,
        mismatchDescription: Description?
    ) {
        matcher.describeMismatch(item?.session, mismatchDescription)
    }

    override fun matchesSafely(item: HolderOrchestrator?): Boolean = matcher.matches(
        item?.session
    )
}
