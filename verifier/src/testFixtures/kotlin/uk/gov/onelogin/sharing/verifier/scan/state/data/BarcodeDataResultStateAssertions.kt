package uk.gov.onelogin.sharing.verifier.scan.state.data

import android.net.Uri
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultAssertions.hasFound
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultAssertions.isNotFound

/**
 * Wrapper object containing [Matcher] functions that map to [BarcodeDataResultState.State] objects.
 */
object BarcodeDataResultStateAssertions {
    fun hasNoBarcodeData() = hasBarcodeData(isNotFound())
    fun hasBarcodeData(expected: String) = hasBarcodeData(hasFound(expected))
    fun hasBarcodeData(matcher: Matcher<BarcodeDataResult>): Matcher<BarcodeDataResultState.State> =
        HasBarcodeScanData(matcher)
}
