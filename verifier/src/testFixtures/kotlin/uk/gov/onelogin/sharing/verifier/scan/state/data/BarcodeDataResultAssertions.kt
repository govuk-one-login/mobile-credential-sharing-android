package uk.gov.onelogin.sharing.verifier.scan.state.data

import android.net.Uri
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher

/**
 * Wrapper object for [Matcher] assertions against a [BarcodeDataResult] object.
 */
object BarcodeDataResultAssertions {
    fun isNotFound() = hasFound(BarcodeDataResult.NotFound)
    fun hasFound(expected: String) = hasFound(BarcodeDataResult.Valid(expected))
    fun hasFound(data: BarcodeDataResult): Matcher<BarcodeDataResult> = equalTo(data)
}
