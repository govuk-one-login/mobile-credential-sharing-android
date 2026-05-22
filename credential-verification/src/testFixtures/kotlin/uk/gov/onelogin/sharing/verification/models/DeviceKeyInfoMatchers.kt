package uk.gov.onelogin.sharing.verification.models

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object DeviceKeyInfoMatchers {
    fun hasDeviceKey(expected: ByteArray) = hasDeviceKey(equalTo(expected))

    fun hasDeviceKey(matcher: Matcher<in ByteArray>): Matcher<in DeviceKeyInfo> =
        DeviceKeyInfoMatcher(matcher) { it?.deviceKey }

    fun hasKeyAuthorizations(expected: Map<String, Map<Int, ByteArray>>) =
        hasKeyAuthorizations(equalTo(expected))

    fun hasKeyAuthorizations(
        matcher: Matcher<in Map<String, Map<Int, ByteArray>>>
    ): Matcher<in DeviceKeyInfo> = DeviceKeyInfoMatcher(matcher) { it?.keyAuthorizations }

    private class DeviceKeyInfoMatcher<Type>(
        private val matcher: Matcher<in Type>,
        private val transformer: (DeviceKeyInfo?) -> Any?
    ) : TypeSafeMatcher<DeviceKeyInfo>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: DeviceKeyInfo?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)
        override fun matchesSafely(item: DeviceKeyInfo?): Boolean =
            matcher.matches(transformer(item))
    }
}
