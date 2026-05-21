package uk.gov.onelogin.sharing.verification

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher

object DeviceKeyInfoMatchers {
    fun hasDeviceKey(
        expected: ByteArray
    ) = hasDeviceKey(equalTo(expected))

    fun hasDeviceKey(
        matcher: Matcher<in ByteArray>
    ): Matcher<in DeviceKeyInfo> = DeviceKeyInfoMatcher(matcher) { it?.deviceKey }

    fun hasKeyAuthorizations(
        expected: Map<String, Map<Int, ByteArray>>
    ) = hasKeyAuthorizations(equalTo(expected))

    fun hasKeyAuthorizations(
        matcher: Matcher<in Map<String, Map<Int, ByteArray>>>
    ): Matcher<in DeviceKeyInfo> = DeviceKeyInfoMatcher(matcher) { it?.keyAuthorizations }
}

