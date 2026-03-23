package uk.gov.onelogin.sharing.verifier.connect

import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.verifier.connect.matchers.HasBluetoothEnabled
import uk.gov.onelogin.sharing.verifier.connect.matchers.HasPreviouslyRequestedPermission

object ConnectWithHolderDeviceStateMatchers {
    fun hasBluetoothDisabled() = hasBluetoothEnabled(false)

    fun hasBluetoothEnabled(expected: Boolean = true): Matcher<ConnectWithHolderDeviceState> =
        HasBluetoothEnabled(expected)

    fun hasNotPreviouslyRequestedPermission() = hasPreviouslyRequestedPermission(false)
    fun hasPreviouslyRequestedPermission(
        hasRequestedPermission: Boolean = true
    ): Matcher<ConnectWithHolderDeviceState> =
        HasPreviouslyRequestedPermission(hasRequestedPermission)
}
