package uk.gov.onelogin.sharing.verifier.scan.state.data

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

/**
 * [TypeSafeMatcher] implementation that maps to the latest value of the
 * [BarcodeDataResultState.State.barcodeDataResult] [kotlinx.coroutines.flow.StateFlow].
 */
internal class HasBarcodeScanData(private val matcher: Matcher<BarcodeDataResult>) :
    TypeSafeMatcher<BarcodeDataResultState.State>() {
    override fun matchesSafely(item: BarcodeDataResultState.State?): Boolean =
        matcher.matches(item?.barcodeDataResult?.value)

    override fun describeTo(description: Description?) {
        matcher.describeTo(description)
    }

    override fun describeMismatchSafely(
        item: BarcodeDataResultState.State?,
        mismatchDescription: Description?
    ) {
        matcher.describeMismatch(item?.barcodeDataResult?.value, mismatchDescription)
    }
}
