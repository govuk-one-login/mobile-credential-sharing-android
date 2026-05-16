package uk.gov.onelogin.sharing.cryptoService.cbor.dto

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.sessiondata.HasData
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.sessiondata.HasStatus
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus

object SessionDataDtoMatchers {
    fun hasData(expected: ByteArray) = hasData(equalTo(expected))

    fun hasData(matcher: Matcher<in ByteArray>): Matcher<in SessionDataDto> = HasData(matcher)

    fun hasStatus(expected: UInt) = hasStatus(equalTo(expected))

    fun hasStatus(status: SessionDataStatus) = hasStatus(status.code)

    fun hasStatus(matcher: Matcher<in UInt>): Matcher<in SessionDataDto> = HasStatus(matcher)
}
