package uk.gov.onelogin.sharing.verification.document.models

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object MobileSecurityObjectMatchers {
    fun hasDeviceKeyInfo(expected: DeviceKeyInfo) = hasDeviceKeyInfo(equalTo(expected))
    fun hasDeviceKeyInfo(matcher: Matcher<in DeviceKeyInfo>): Matcher<in MobileSecurityObject> =
        MobileSecurityObjectMatcher(matcher) {
            it?.deviceKeyInfo
        }
    fun hasDigestAlgorithm(expected: String) = hasDigestAlgorithm(equalTo(expected))
    fun hasDigestAlgorithm(matcher: Matcher<in String>): Matcher<in MobileSecurityObject> =
        MobileSecurityObjectMatcher(matcher) {
            it?.digestAlgorithm
        }
    fun hasDocType(expected: String) = hasDocType(equalTo(expected))
    fun hasDocType(matcher: Matcher<in String>): Matcher<in MobileSecurityObject> =
        MobileSecurityObjectMatcher(matcher) {
            it?.docType
        }
    fun hasStatus(expected: ByteArray?) = hasStatus(equalTo(expected))
    fun hasStatus(matcher: Matcher<in ByteArray>): Matcher<in MobileSecurityObject> =
        MobileSecurityObjectMatcher(matcher) {
            it?.status
        }
    fun hasValidityInfo(expected: ValidityInfo) = hasValidityInfo(equalTo(expected))
    fun hasValidityInfo(matcher: Matcher<in ValidityInfo>): Matcher<in MobileSecurityObject> =
        MobileSecurityObjectMatcher(matcher) {
            it?.validityInfo
        }
    fun hasValueDigests(expected: Map<String, Map<Int, ByteArray>>) =
        hasValueDigests(equalTo(expected))
    fun hasValueDigests(
        matcher: Matcher<in Map<String, Map<Int, ByteArray>>>
    ): Matcher<in MobileSecurityObject> = MobileSecurityObjectMatcher(matcher) {
        it?.valueDigests
    }
    fun hasVersion(expected: String) = hasVersion(equalTo(expected))
    fun hasVersion(matcher: Matcher<in String>): Matcher<in MobileSecurityObject> =
        MobileSecurityObjectMatcher(matcher) {
            it?.version
        }

    private class MobileSecurityObjectMatcher<Type>(
        private val matcher: Matcher<in Type>,
        private val transformer: (MobileSecurityObject?) -> Type?
    ) : TypeSafeMatcher<MobileSecurityObject>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: MobileSecurityObject?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)
        override fun matchesSafely(item: MobileSecurityObject?): Boolean =
            matcher.matches(transformer(item))
    }
}
