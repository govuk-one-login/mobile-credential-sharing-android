package uk.gov.onelogin.sharing.cryptoService.cbor.dto.deviceengagement

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceEngagementDto
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceRetrievalMethodDto

internal class HasDeviceRetrievalMethods(
    private val matcher: Matcher<in List<DeviceRetrievalMethodDto>>
) : TypeSafeMatcher<DeviceEngagementDto>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)
    override fun describeMismatchSafely(
        item: DeviceEngagementDto?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(item?.deviceRetrievalMethods, mismatchDescription)

    override fun matchesSafely(item: DeviceEngagementDto?): Boolean =
        matcher.matches(item?.deviceRetrievalMethods)
}
