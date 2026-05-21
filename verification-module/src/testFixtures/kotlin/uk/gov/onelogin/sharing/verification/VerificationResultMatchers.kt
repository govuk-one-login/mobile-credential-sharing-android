package uk.gov.onelogin.sharing.verification

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher

object VerificationResultMatchers {
    fun hasError(
        expected: VerificationError
    ): Matcher<in VerificationResult.Failure> = hasError(equalTo(expected))

    fun hasError(
        matcher: Matcher<in VerificationError>
    ): Matcher<in VerificationResult.Failure> = HasError(matcher)
}
