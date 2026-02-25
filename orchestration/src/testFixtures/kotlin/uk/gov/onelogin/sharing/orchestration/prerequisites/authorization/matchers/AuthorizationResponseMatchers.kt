package uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.matchers

import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.AuthorizationResponse
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.UnauthorizedReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.matchers.UnauthorizedReasonMatchers.isMissingPermissions

object AuthorizationResponseMatchers {
    fun hasUnauthorizedPermissions(
        matcher: Matcher<in Iterable<String>>
    ): Matcher<AuthorizationResponse> = hasUnauthorizedReason(
        isMissingPermissions(
            matcher
        )
    )

    fun hasUnauthorizedReason(
        matcher: Matcher<in UnauthorizedReason>
    ): Matcher<AuthorizationResponse> = HasUnauthorizedReason(matcher)
}
