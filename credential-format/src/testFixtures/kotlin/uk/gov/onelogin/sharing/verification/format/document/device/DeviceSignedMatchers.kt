package uk.gov.onelogin.sharing.verification.format.document.device

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object DeviceSignedMatchers {
    fun hasDeviceNameSpaceBytes(expected: ByteArray) = hasDeviceNameSpaceBytes(equalTo(expected))
    fun hasDeviceNameSpaceBytes(matcher: Matcher<in ByteArray>): Matcher<in DeviceSigned> =
        DeviceSignedMatcher(matcher) {
            it?.deviceNameSpacesBytes
        }
    fun hasDeviceSignature(expected: ByteArray) = hasDeviceSignature(equalTo(expected))
    fun hasDeviceSignature(matcher: Matcher<in ByteArray>): Matcher<in DeviceSigned> =
        DeviceSignedMatcher(matcher) {
            it?.deviceSignature
        }

    private class DeviceSignedMatcher<Type>(
        private val matcher: Matcher<in Type>,
        private val transformer: (DeviceSigned?) -> Type?
    ) : TypeSafeMatcher<DeviceSigned>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: DeviceSigned?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)
        override fun matchesSafely(item: DeviceSigned?): Boolean =
            matcher.matches(transformer(item))
    }
}
