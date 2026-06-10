package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates

object PeripheralBluetoothStateMatchers {
    fun hasAddress(
        expected: String
    ) = hasAddress(equalTo(expected))

    fun hasAddress(
        matcher: Matcher<in String>
    ): Matcher<PeripheralBluetoothState> = PeripheralBluetoothStateMatcher(
        matcher
    ) {
        (it as? PeripheralBluetoothState.HasDeviceAddress)?.address
    }

    fun hasMessage(
        expected: ByteArray
    ) = hasMessage(equalTo(expected))

    fun hasMessage(
        matcher: Matcher<in ByteArray>
    ): Matcher<in PeripheralBluetoothState> = PeripheralBluetoothStateMatcher(matcher) {
        (it as? PeripheralBluetoothState.MessageReceived)?.message
    }

    fun hasSessionEnd(
        expected: Boolean
    ) = hasSessionEnd(equalTo(expected))

    fun hasSessionEnd(
        matcher: Matcher<in Boolean>
    ): Matcher<in PeripheralBluetoothState> = PeripheralBluetoothStateMatcher(matcher) {
        (it as? PeripheralBluetoothState.Disconnected)?.isSessionEnd
    }

    fun hasStatus(
        expected: SessionEndStates
    ) = hasStatus(equalTo(expected))

    fun hasStatus(
        matcher: Matcher<in SessionEndStates>
    ): Matcher<in PeripheralBluetoothState> = PeripheralBluetoothStateMatcher(matcher) {
        (it as? PeripheralBluetoothState.Ended)?.status
    }

    fun hasTransportError(
        expected: PeripheralBluetoothTransportError
    ) = hasTransportError(equalTo(expected))

    fun hasTransportError(
        matcher: Matcher<in PeripheralBluetoothTransportError>
    ): Matcher<in PeripheralBluetoothState> = PeripheralBluetoothStateMatcher(matcher) {
        (it as? PeripheralBluetoothState.Error)?.reason
    }

    fun isConnected(
        expected: PeripheralBluetoothState.Connected
    ) = isConnected(equalTo(expected))

    fun isConnected(
        matcher: Matcher<in PeripheralBluetoothState.Connected>
    ): Matcher<PeripheralBluetoothState> = PeripheralBluetoothStateMatcher(matcher) {
        (it as? PeripheralBluetoothState.Connected)
    }

    fun isDisconnected(
        expected: PeripheralBluetoothState.Disconnected
    ) = isDisconnected(equalTo(expected))

    fun isDisconnected(
        matcher: Matcher<in PeripheralBluetoothState.Disconnected>
    ): Matcher<in PeripheralBluetoothState> = PeripheralBluetoothStateMatcher(
        matcher
    ) {
        (it as? PeripheralBluetoothState.Disconnected)
    }

    fun isEnded(
        expected: PeripheralBluetoothState.Ended
    ) = isEnded(equalTo(expected))

    fun isEnded(
        matcher: Matcher<in PeripheralBluetoothState.Ended>
    ): Matcher<in PeripheralBluetoothState> = PeripheralBluetoothStateMatcher(matcher) {
        (it as? PeripheralBluetoothState.Ended)
    }

    fun isError(
        expected: PeripheralBluetoothState.Error
    ) = isError(equalTo(expected))

    fun isError(
        expected: PeripheralBluetoothTransportError
    ) = isError(hasTransportError(expected))

    fun isError(
        matcher: Matcher<in PeripheralBluetoothState.Error>
    ): Matcher<in PeripheralBluetoothState> = PeripheralBluetoothStateMatcher(matcher) {
        (it as? PeripheralBluetoothState.Error)
    }

    fun isMessageReceived(
        expected: PeripheralBluetoothState.MessageReceived
    ) = isMessageReceived(equalTo(expected))

    fun isMessageReceived(
        matcher: Matcher<in PeripheralBluetoothState.MessageReceived>
    ): Matcher<in PeripheralBluetoothState> = PeripheralBluetoothStateMatcher(matcher) {
        (it as? PeripheralBluetoothState.MessageReceived)
    }

    private class PeripheralBluetoothStateMatcher<Type>(
        private val matcher: Matcher<Type>,
        private val transformer: (PeripheralBluetoothState?) -> Type?
    ) : TypeSafeMatcher<PeripheralBluetoothState>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: PeripheralBluetoothState?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)
        override fun matchesSafely(item: PeripheralBluetoothState?): Boolean = matcher.matches(
            transformer(item)
        )
    }
}