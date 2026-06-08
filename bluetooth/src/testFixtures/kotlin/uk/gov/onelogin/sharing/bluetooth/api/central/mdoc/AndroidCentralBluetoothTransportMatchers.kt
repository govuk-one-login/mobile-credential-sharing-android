package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import kotlinx.coroutines.Job
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.hamcrest.TypeSafeMatcher

object AndroidCentralBluetoothTransportMatchers {
    fun hasMonitoringJob(
        matcher: Matcher<in Job> = not(nullValue())
    ): Matcher<in AndroidCentralBluetoothTransport> = AndroidCentralBluetoothTransportMatcher(
        matcher
    ) { it?.monitoringJob }

    fun hasScanJob(
        matcher: Matcher<in Job> = not(nullValue())
    ): Matcher<in AndroidCentralBluetoothTransport> = AndroidCentralBluetoothTransportMatcher(
        matcher
    ) { it?.scanJob }

    private class AndroidCentralBluetoothTransportMatcher<Type>(
        private val matcher: Matcher<Type>,
        private val transformer: (AndroidCentralBluetoothTransport?) -> Type?
    ) : TypeSafeMatcher<AndroidCentralBluetoothTransport>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: AndroidCentralBluetoothTransport?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)
        override fun matchesSafely(item: AndroidCentralBluetoothTransport?): Boolean =
            matcher.matches(
                transformer(item)
            )
    }
}
