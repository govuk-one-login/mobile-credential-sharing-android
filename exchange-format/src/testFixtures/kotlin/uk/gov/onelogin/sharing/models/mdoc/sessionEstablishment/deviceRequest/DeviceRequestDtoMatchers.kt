package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object DeviceRequestDtoMatchers {

    fun hasDeviceRequestInfo(expected: ByteArray) = hasDeviceRequestInfo(equalTo(expected))

    fun hasDeviceRequestInfo(matcher: Matcher<in ByteArray>): Matcher<in DeviceRequestDto> =
        DeviceRequestDtoMatcher(matcher) {
            it?.deviceRequestInfo
        }

    fun hasDocumentRequests(expected: List<DocRequestDto>) = hasDocumentRequests(equalTo(expected))

    fun hasDocumentRequests(
        matcher: Matcher<in List<DocRequestDto>>
    ): Matcher<in DeviceRequestDto> = DeviceRequestDtoMatcher(matcher) {
        it?.docRequest
    }

    fun hasReaderAuthAll(expected: ByteArray) = hasReaderAuthAll(equalTo(expected))

    fun hasReaderAuthAll(matcher: Matcher<in ByteArray>): Matcher<in DeviceRequestDto> =
        DeviceRequestDtoMatcher(matcher) {
            it?.readerAuthAll
        }

    fun hasVersion(expected: String) = hasVersion(equalTo(expected))

    fun hasVersion(matcher: Matcher<in String>): Matcher<in DeviceRequestDto> =
        DeviceRequestDtoMatcher(matcher) {
            it?.version
        }

    private class DeviceRequestDtoMatcher<Type>(
        private val matcher: Matcher<in Type>,
        private val transformer: (DeviceRequestDto?) -> Type?
    ) : TypeSafeMatcher<DeviceRequestDto>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: DeviceRequestDto?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)
        override fun matchesSafely(item: DeviceRequestDto?): Boolean = matcher.matches(
            transformer(item)
        )
    }
}
