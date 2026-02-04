package uk.gov.onelogin.sharing.orchestration.session.holder

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

/**
 * Custom [HolderSession] hamcrest [Matcher] that asserts against [HolderSession.currentState].
 */
internal class HasCurrentState(private val matcher: Matcher<HolderSessionState>) :
    TypeSafeMatcher<HolderSession>() {

    override fun describeTo(description: Description?) {
        matcher.describeTo(description)
    }

    override fun describeMismatchSafely(item: HolderSession?, mismatchDescription: Description?) {
        matcher.describeMismatch(item?.currentState?.value, mismatchDescription)
    }

    override fun matchesSafely(item: HolderSession?): Boolean = matcher.matches(
        item?.currentState?.value
    )
}
