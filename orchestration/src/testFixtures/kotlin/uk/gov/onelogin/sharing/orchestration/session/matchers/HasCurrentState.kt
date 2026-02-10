package uk.gov.onelogin.sharing.orchestration.session.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.session.StateContainer
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSession

/**
 * Custom [HolderSession] hamcrest [Matcher] that asserts against [HolderSession.currentState].
 */
internal class HasCurrentState<State : Any>(private val matcher: Matcher<in State>) :
    TypeSafeMatcher<StateContainer<in State>>() {

    override fun describeTo(description: Description?) {
        matcher.describeTo(description)
    }

    override fun describeMismatchSafely(
        item: StateContainer<in State>?,
        mismatchDescription: Description?
    ) {
        matcher.describeMismatch(item?.currentState?.value, mismatchDescription)
    }

    override fun matchesSafely(item: StateContainer<in State>?): Boolean = matcher.matches(
        item?.currentState?.value
    )
}
