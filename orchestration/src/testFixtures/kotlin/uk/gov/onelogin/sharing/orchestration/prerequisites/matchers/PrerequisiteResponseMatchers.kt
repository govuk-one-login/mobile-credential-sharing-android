package uk.gov.onelogin.sharing.orchestration.prerequisites.matchers

import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.UnauthorizedReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.UnauthorizedReasonMatchers.isMissingPermissions
import uk.gov.onelogin.sharing.orchestration.prerequisites.capability.IncapableReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.readiness.NotReadyReason

object PrerequisiteResponseMatchers {

    fun hasUnauthorizedPermissions(
        matcher: Matcher<in Iterable<String>>
    ): Matcher<MissingPrerequisiteReason> = hasUnauthorizedReason(
        isMissingPermissions(
            matcher
        )
    )

    fun hasIncapableReason(
        matcher: Matcher<in IncapableReason>
    ): Matcher<MissingPrerequisiteReason> = HasIncapableReason(matcher)

    fun hasNotReadyReason(matcher: Matcher<in NotReadyReason>): Matcher<MissingPrerequisiteReason> =
        HasNotReadyReason(matcher)

    fun hasUnauthorizedReason(
        matcher: Matcher<in UnauthorizedReason>
    ): Matcher<MissingPrerequisiteReason> = HasUnauthorizedReason(matcher)
}
