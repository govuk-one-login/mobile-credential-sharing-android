package uk.gov.onelogin.sharing.models.mdoc.engagment

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethodDto
import uk.gov.onelogin.sharing.models.mdoc.security.SecurityDto

object DeviceEngagementDtoMatchers {
    fun hasVersion(expected: String) = hasVersion(equalTo(expected))

    fun hasVersion(matcher: Matcher<in String>): Matcher<in DeviceEngagementDto> =
        HasVersion(matcher)

    fun hasSecurity(expected: SecurityDto) = hasSecurity(equalTo(expected))

    fun hasSecurity(matcher: Matcher<in SecurityDto>): Matcher<in DeviceEngagementDto> =
        HasSecurity(matcher)

    fun hasDeviceRetrievalMethods(expected: List<DeviceRetrievalMethodDto>) =
        hasDeviceRetrievalMethods(equalTo(expected))

    fun hasDeviceRetrievalMethods(
        matcher: Matcher<in List<DeviceRetrievalMethodDto>>
    ): Matcher<in DeviceEngagementDto> = HasDeviceRetrievalMethods(matcher)
}
