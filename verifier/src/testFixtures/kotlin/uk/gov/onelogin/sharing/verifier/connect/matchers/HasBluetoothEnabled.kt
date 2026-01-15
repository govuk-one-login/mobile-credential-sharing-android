package uk.gov.onelogin.sharing.verifier.connect.matchers

import kotlin.math.exp
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceEngagementDto
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceState

class HasBluetoothEnabled(private val expected: Boolean) :
    TypeSafeMatcher<ConnectWithHolderDeviceState>() {

    override fun describeMismatchSafely(
        item: ConnectWithHolderDeviceState?,
        mismatchDescription: Description?
    ) {
        mismatchDescription?.appendText("${item?.isBluetoothEnabled}")
    }

    override fun describeTo(description: Description?) {
        description?.appendText("$expected")
    }

    override fun matchesSafely(item: ConnectWithHolderDeviceState?): Boolean =
        expected == item?.isBluetoothEnabled
}
