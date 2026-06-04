package uk.gov.onelogin.sharing.core.permission

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.core.permission.PermissionChecker.PermissionCheckResult
import uk.gov.onelogin.sharing.core.permission.PermissionChecker.PermissionCheckResult.Denied
import uk.gov.onelogin.sharing.core.permission.PermissionChecker.PermissionCheckResult.PermanentlyDenied
import uk.gov.onelogin.sharing.core.permission.PermissionChecker.PermissionCheckResult.Undetermined

object PermissionCheckResultMatchers {
    fun hasPermission(expected: String) = hasPermission(equalTo(expected))

    fun hasPermission(matcher: Matcher<String>): Matcher<in PermissionCheckResult> =
        PermissionCheckResultMatcher(
            matcher
        ) {
            it?.permission
        }

    fun isDenied(): Matcher<in PermissionCheckResult> = instanceOf(Denied::class.java)

    fun isPermanentlyDenied(): Matcher<in PermissionCheckResult> =
        instanceOf(PermanentlyDenied::class.java)

    fun isUndetermined(): Matcher<in PermissionCheckResult> = instanceOf(Undetermined::class.java)

    private class PermissionCheckResultMatcher<Type>(
        private val matcher: Matcher<Type>,
        private val transformer: (PermissionCheckResult?) -> Type?
    ) : TypeSafeMatcher<PermissionCheckResult>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: PermissionCheckResult?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)
        override fun matchesSafely(item: PermissionCheckResult?): Boolean =
            matcher.matches(transformer(item))
    }
}
