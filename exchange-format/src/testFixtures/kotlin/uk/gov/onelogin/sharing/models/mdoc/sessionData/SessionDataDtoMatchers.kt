package uk.gov.onelogin.sharing.models.mdoc.sessionData

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher

object SessionDataDtoMatchers {
    fun hasData(expected: ByteArray) = hasData(equalTo(expected))

    fun hasData(matcher: Matcher<in ByteArray>): Matcher<in SessionDataDto> = HasData(matcher)

    fun hasStatus(expected: UInt) = hasStatus(equalTo(expected))

    fun hasStatus(status: SessionDataStatus) = hasStatus(status.code)

    fun hasStatus(matcher: Matcher<in UInt>): Matcher<in SessionDataDto> = HasStatus(matcher)
}
