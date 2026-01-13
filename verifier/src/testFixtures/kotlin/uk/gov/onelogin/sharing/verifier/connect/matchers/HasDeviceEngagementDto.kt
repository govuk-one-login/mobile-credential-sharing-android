package uk.gov.onelogin.sharing.verifier.connect.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceEngagementDto
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceState

class HasDeviceEngagementDto(private val matcher: Matcher<DeviceEngagementDto>) :
    TypeSafeMatcher<ConnectWithHolderDeviceState>() {

    override fun describeMismatchSafely(
        item: ConnectWithHolderDeviceState?,
        mismatchDescription: Description?
    ) {
        matcher.describeMismatch(item?.engagementData, mismatchDescription)
    }

    override fun describeTo(description: Description?) {
        matcher.describeTo(description)
    }

    override fun matchesSafely(item: ConnectWithHolderDeviceState?): Boolean =
        matcher.matches(item?.engagementData)
}
