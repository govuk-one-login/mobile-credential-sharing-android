package uk.gov.onelogin.sharing.verifier.scan

import android.net.Uri
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.verifier.scan.BarcodeDataResultAssertions.hasFoundBarcode
import uk.gov.onelogin.sharing.verifier.scan.BarcodeDataResultAssertions.hasNotFoundBarcode

object VerifierScannerViewModelAssertions {
    fun hasPreviouslyDeniedPermission() = hasPreviouslyDeniedPermission(true)
    fun hasPreviouslyDeniedPermission(expected: Boolean): Matcher<VerifierScannerViewModel> =
        HasPreviouslyDeniedPermission(expected)

    fun hasPreviouslyGrantedPermission() = hasPreviouslyDeniedPermission(false)

    fun hasNullUri() = hasUri(hasNotFoundBarcode())
    fun hasUri(expected: Uri) = hasUri(hasFoundBarcode(expected))
    fun hasUri(matcher: Matcher<BarcodeDataResult>): Matcher<VerifierScannerViewModel> =
        HasUri(matcher)

    fun isInInitialState(): Matcher<VerifierScannerViewModel> = allOf(
        hasPreviouslyGrantedPermission(),
        hasNullUri()
    )
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

internal class HasUri(private val matcher: Matcher<BarcodeDataResult>) :
    TypeSafeMatcher<VerifierScannerViewModel>() {
    override fun matchesSafely(item: VerifierScannerViewModel?): Boolean =
        matcher.matches(item?.uri?.value)

    override fun describeTo(description: Description?) {
        matcher.describeTo(description)
    }

    override fun describeMismatchSafely(
        item: VerifierScannerViewModel?,
        mismatchDescription: Description?
    ) {
        matcher.describeMismatch(item?.uri?.value, mismatchDescription)
    }
}
