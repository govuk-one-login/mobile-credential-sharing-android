package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import kotlinx.coroutines.Job
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object AndroidPeripheralBluetoothTransportMatchers {
    fun hasMonitoringJob(
        matcher: Matcher<in Job>
    ): Matcher<in AndroidPeripheralBluetoothTransport> =
        AndroidPeripheralBluetoothTransportMatcher(matcher) {
            it?.monitoringJob
        }

    private class AndroidPeripheralBluetoothTransportMatcher<Type>(
        private val matcher: Matcher<Type>,
        private val transformer: (AndroidPeripheralBluetoothTransport?) -> Type?
    ) : TypeSafeMatcher<AndroidPeripheralBluetoothTransport>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: AndroidPeripheralBluetoothTransport?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)

        override fun matchesSafely(item: AndroidPeripheralBluetoothTransport?): Boolean =
            matcher.matches(transformer(item))
    }
}
