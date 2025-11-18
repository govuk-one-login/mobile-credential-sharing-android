package uk.gov.onelogin.sharing.verifier.scan

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object VerifierScannerViewModelAssertions {
    fun hasPreviouslyDeniedPermission() = hasPreviouslyDeniedPermission(true)
    fun hasPreviouslyDeniedPermission(expected: Boolean): Matcher<VerifierScannerViewModel> =
        HasPreviouslyDeniedPermission(expected)

    fun hasPreviouslyGrantedPermission() = hasPreviouslyDeniedPermission(false)
}

internal class HasPreviouslyDeniedPermission(private val expected: Boolean) :
    TypeSafeMatcher<VerifierScannerViewModel>() {
    override fun matchesSafely(item: VerifierScannerViewModel?): Boolean =
        expected == item?.hasPreviouslyDeniedPermission?.value

    override fun describeTo(description: Description?) {
        description?.appendText("has previously denied permission: $expected")
    }

    override fun describeMismatchSafely(
        item: VerifierScannerViewModel?,
        mismatchDescription: Description?
    ) {
        mismatchDescription?.appendValue(item?.hasPreviouslyDeniedPermission?.value)
    }
}
