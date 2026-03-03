package uk.gov.onelogin.sharing.orchestration.prerequisites.matchers

import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.UnauthorizedReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.UnauthorizedReasonMatchers.isMissingPermissions

object PrerequisiteResponseMatchers {

    fun hasUnauthorizedPermissions(
        matcher: Matcher<in Iterable<String>>,
    ): Matcher<PrerequisiteResponse> = hasUnauthorizedReason(
        isMissingPermissions(
            matcher
        )
    )

    fun hasUnauthorizedReason(
        matcher: Matcher<in UnauthorizedReason>,
    ): Matcher<PrerequisiteResponse> = HasUnauthorizedReason(matcher)
}
