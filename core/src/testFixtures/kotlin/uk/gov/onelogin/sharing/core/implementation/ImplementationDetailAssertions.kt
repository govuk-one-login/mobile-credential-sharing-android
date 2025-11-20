package uk.gov.onelogin.sharing.core.implementation

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.core.implementation.assertions.HasDescription
import uk.gov.onelogin.sharing.core.implementation.assertions.HasTicket

object ImplementationDetailAssertions {
    fun hasDescription(expected: String) = hasDescription(equalTo(expected))
    fun hasDescription(matcher: Matcher<String>): Matcher<ImplementationDetail> =
        HasDescription(matcher)

    fun hasTicket(expected: String) = hasTicket(equalTo(expected))
    fun hasTicket(matcher: Matcher<String>): Matcher<ImplementationDetail> = HasTicket(matcher)
}
