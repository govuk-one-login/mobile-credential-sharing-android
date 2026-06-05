package uk.gov.onelogin.sharing.cryptoService.cbor.dto

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.deviceengagement.HasDeviceRetrievalMethods
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.deviceengagement.HasSecurity
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.deviceengagement.HasVersion

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
