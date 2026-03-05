package uk.gov.onelogin.sharing.orchestration.prerequisites.matchers

import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.UnauthorizedReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.UnauthorizedReasonMatchers.isMissingPermissions
import uk.gov.onelogin.sharing.orchestration.prerequisites.capability.IncapableReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.readiness.NotReadyReason

object PrerequisiteResponseMatchers {

    fun hasUnauthorizedPermissions(
        matcher: Matcher<in Iterable<String>>
    ): Matcher<PrerequisiteResponse> = hasUnauthorizedReason(
        isMissingPermissions(
            matcher
        )
    )

    fun hasIncapableReason(matcher: Matcher<in IncapableReason>): Matcher<PrerequisiteResponse> =
        HasIncapableReason(matcher)

    fun hasNotReadyReason(matcher: Matcher<in NotReadyReason>): Matcher<PrerequisiteResponse> =
        HasNotReadyReason(matcher)

    fun hasUnauthorizedReason(
        matcher: Matcher<in UnauthorizedReason>
    ): Matcher<PrerequisiteResponse> = HasUnauthorizedReason(matcher)
}
