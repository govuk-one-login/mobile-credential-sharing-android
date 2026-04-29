package uk.gov.onelogin.sharing.verifier.connect

import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.verifier.connect.matchers.HasBluetoothEnabled

object ConnectWithHolderDeviceStateMatchers {
    fun hasBluetoothDisabled() = hasBluetoothEnabled(false)

    fun hasBluetoothEnabled(expected: Boolean = true): Matcher<ConnectWithHolderDeviceState> =
        HasBluetoothEnabled(expected)
}
