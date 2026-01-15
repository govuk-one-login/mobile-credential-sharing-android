package uk.gov.onelogin.sharing.verifier.connect.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceEngagementDto
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceState

class HasPreviouslyRequestedPermission(private val expected: Boolean) :
    TypeSafeMatcher<ConnectWithHolderDeviceState>() {

    override fun describeMismatchSafely(
        item: ConnectWithHolderDeviceState?,
        mismatchDescription: Description?
    ) {
        mismatchDescription?.appendValue(item?.hasRequestedPermissions)
    }

    override fun describeTo(description: Description?) {
        description?.appendValue(expected)
    }

    override fun matchesSafely(item: ConnectWithHolderDeviceState?): Boolean =
        expected == item?.hasRequestedPermissions
}
