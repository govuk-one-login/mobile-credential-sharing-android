package uk.gov.onelogin.sharing.verification.models

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

internal class DeviceKeyInfoMatcher<Type>(
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