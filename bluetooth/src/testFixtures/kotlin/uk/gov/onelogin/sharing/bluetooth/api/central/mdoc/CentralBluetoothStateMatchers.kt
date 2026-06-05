package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import java.util.UUID
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates

object CentralBluetoothStateMatchers {

    fun hasAddress(
        expected: String,
    ) = hasAddress(equalTo(expected))

    fun hasAddress(
        matcher: Matcher<in String>,
    ): Matcher<in CentralBluetoothState> = CentralBluetoothStateMatcher(
        matcher
    ) {
        (it as? CentralBluetoothState.HasDeviceAddress)?.address
    }

    fun hasSessionEnd(
        expected: Boolean,
    ) = hasSessionEnd(equalTo(expected))

    fun hasSessionEnd(
        matcher: Matcher<in Boolean>,
    ): Matcher<in CentralBluetoothState> = CentralBluetoothStateMatcher(matcher) {
        (it as? CentralBluetoothState.Disconnected)?.isSessionEnd
    }

    fun hasUuid(
        expected: UUID
    ) = hasUuid(equalTo(expected))

    fun hasUuid(
        matcher: Matcher<in UUID>
    ): Matcher<in CentralBluetoothState.Message> = CentralBluetoothStateMatcher(matcher) {
        (it as? CentralBluetoothState.Message)?.uuid
    }

    fun hasValue(
        expected: ByteArray
    ) = hasValue(equalTo(expected))

    fun hasValue(
        matcher: Matcher<in ByteArray>
    ): Matcher<in CentralBluetoothState.Message> = CentralBluetoothStateMatcher(matcher) {
        (it as? CentralBluetoothState.Message)?.value
    }

    fun isBluetoothEnded(
        matcher: Matcher<in SessionEndStates>,
    ): Matcher<in CentralBluetoothState> = CentralBluetoothStateMatcher(matcher) {
        (it as? CentralBluetoothState.CentralBluetoothEnded)?.status
    }

    fun isConnected(
        matcher: Matcher<in CentralBluetoothState.Connected>,
    ): Matcher<in CentralBluetoothState> = CentralBluetoothStateMatcher(matcher) {
        (it as? CentralBluetoothState.Connected)
    }

    fun isDisconnected(
        matcher: Matcher<in CentralBluetoothState.Disconnected>,
    ): Matcher<in CentralBluetoothState> = CentralBluetoothStateMatcher(matcher) {
        (it as? CentralBluetoothState.Disconnected)
    }

    fun isError(
        expected: CentralBluetoothTransportError,
    ): Matcher<in CentralBluetoothState> = isError(equalTo(expected))

    fun isError(
        matcher: Matcher<in CentralBluetoothTransportError>,
    ): Matcher<in CentralBluetoothState> = CentralBluetoothStateMatcher(matcher) {
        (it as? CentralBluetoothState.Error)?.reason
    }

    fun isMessage(
        matcher: Matcher<in CentralBluetoothState.Message>,
    ): Matcher<in CentralBluetoothState> = CentralBluetoothStateMatcher(matcher) {
        (it as? CentralBluetoothState.Message)
    }

    private class CentralBluetoothStateMatcher<Type>(
        private val matcher: Matcher<Type>,
        private val transformer: (CentralBluetoothState?) -> Type?,
    ) : TypeSafeMatcher<CentralBluetoothState>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: CentralBluetoothState?,
            mismatchDescription: Description?,
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)

        override fun matchesSafely(item: CentralBluetoothState?): Boolean = matcher.matches(
            transformer(item)
        )
    }

}