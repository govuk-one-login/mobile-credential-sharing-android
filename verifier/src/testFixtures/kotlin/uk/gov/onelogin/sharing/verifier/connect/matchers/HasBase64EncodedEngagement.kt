package uk.gov.onelogin.sharing.verifier.connect.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceState

class HasBase64EncodedEngagement(private val matcher: Matcher<String>) :
    TypeSafeMatcher<ConnectWithHolderDeviceState>() {

    override fun describeMismatchSafely(
        item: ConnectWithHolderDeviceState?,
        mismatchDescription: Description?
    ) {
        matcher.describeMismatch(item?.base64EncodedEngagement, mismatchDescription)
    }

    override fun describeTo(description: Description?) {
        matcher.describeTo(description)
    }

    override fun matchesSafely(item: ConnectWithHolderDeviceState?): Boolean =
        matcher.matches(item?.base64EncodedEngagement)
}
