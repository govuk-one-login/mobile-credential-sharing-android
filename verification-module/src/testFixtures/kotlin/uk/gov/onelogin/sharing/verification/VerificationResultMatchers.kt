package uk.gov.onelogin.sharing.verification

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.verification.result.VerificationError
import uk.gov.onelogin.sharing.verification.result.VerificationResult

object VerificationResultMatchers {
    fun hasError(
        expected: VerificationError
    ): Matcher<in VerificationResult> = hasError(equalTo(expected))

    fun hasError(
        matcher: Matcher<in VerificationError>
    ): Matcher<in VerificationResult> = VerificationResultMatcher(matcher) {
        (it as? VerificationResult.Failure)?.error
    }
}
