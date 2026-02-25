package uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.matchers

import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.UnauthorizedReason

object UnauthorizedReasonMatchers {
    fun isMissingPermissionInstance(): Matcher<UnauthorizedReason> =
        CoreMatchers.instanceOf(UnauthorizedReason.MissingPermissions::class.java)

    fun isMissingPermissions(matcher: Matcher<in Iterable<String>>): Matcher<UnauthorizedReason> =
        IsMissingPermission(matcher)
}
