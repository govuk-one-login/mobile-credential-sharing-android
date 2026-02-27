package uk.gov.onelogin.sharing.verifier.connect

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceEngagementDto
import uk.gov.onelogin.sharing.verifier.connect.matchers.HasBase64EncodedEngagement
import uk.gov.onelogin.sharing.verifier.connect.matchers.HasBluetoothEnabled
import uk.gov.onelogin.sharing.verifier.connect.matchers.HasDeviceEngagementDto
import uk.gov.onelogin.sharing.verifier.connect.matchers.HasPreviouslyRequestedPermission

object ConnectWithHolderDeviceStateMatchers {
    fun hasBase64EncodedEngagement(expected: String) = hasBase64EncodedEngagement(equalTo(expected))

    fun hasBase64EncodedEngagement(
        matcher: Matcher<String>
    ): Matcher<ConnectWithHolderDeviceState> = HasBase64EncodedEngagement(matcher)

    fun hasBluetoothDisabled() = hasBluetoothEnabled(false)

    fun hasBluetoothEnabled(expected: Boolean = true): Matcher<ConnectWithHolderDeviceState> =
        HasBluetoothEnabled(expected)

    fun hasDeviceEngagementDto(expected: DeviceEngagementDto) =
        hasDeviceEngagementDto(equalTo(expected))

    fun hasDeviceEngagementDto(
        matcher: Matcher<DeviceEngagementDto>
    ): Matcher<ConnectWithHolderDeviceState> = HasDeviceEngagementDto(matcher)

    fun hasNotPreviouslyRequestedPermission() = hasPreviouslyRequestedPermission(false)
    fun hasPreviouslyRequestedPermission(
        hasRequestedPermission: Boolean = true
    ): Matcher<ConnectWithHolderDeviceState> =
        HasPreviouslyRequestedPermission(hasRequestedPermission)
}
