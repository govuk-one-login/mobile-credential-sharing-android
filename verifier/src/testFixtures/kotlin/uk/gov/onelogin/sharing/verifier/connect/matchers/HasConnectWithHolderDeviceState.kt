package uk.gov.onelogin.sharing.verifier.connect.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceState
import uk.gov.onelogin.sharing.verifier.connect.SessionEstablishmentViewModel

class HasConnectWithHolderDeviceState(private val matcher: Matcher<ConnectWithHolderDeviceState>) :
    TypeSafeMatcher<SessionEstablishmentViewModel>() {

    override fun describeMismatchSafely(
        item: SessionEstablishmentViewModel?,
        mismatchDescription: Description?
    ) {
        matcher.describeMismatch(item?.uiState?.value, mismatchDescription)
    }

    override fun describeTo(description: Description?) {
        matcher.describeTo(description)
    }

    override fun matchesSafely(item: SessionEstablishmentViewModel?): Boolean =
        matcher.matches(item?.uiState?.value)
}
