package uk.gov.onelogin.sharing.verifier.scan.state.permission

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

/**
 * [TypeSafeMatcher] implementation that maps to the latest value of the
 * [PreviouslyDeniedPermissionState.State.hasPreviouslyDeniedPermission]
 * [kotlinx.coroutines.flow.StateFlow].
 */
internal class HasPreviouslyDeniedPermission(private val expected: Boolean) :
    TypeSafeMatcher<PreviouslyDeniedPermissionState.State>() {
    override fun matchesSafely(item: PreviouslyDeniedPermissionState.State?): Boolean =
        expected == item?.hasPreviouslyDeniedPermission?.value

    override fun describeTo(description: Description?) {
        description?.appendText("has previously denied permission: $expected")
    }

    override fun describeMismatchSafely(
        item: PreviouslyDeniedPermissionState.State?,
        mismatchDescription: Description?
    ) {
        mismatchDescription?.appendValue(item?.hasPreviouslyDeniedPermission?.value)
    }
}
