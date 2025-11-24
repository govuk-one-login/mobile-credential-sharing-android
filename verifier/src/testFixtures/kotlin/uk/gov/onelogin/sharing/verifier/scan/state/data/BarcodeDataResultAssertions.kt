package uk.gov.onelogin.sharing.verifier.scan.state.data

import android.net.Uri
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher

object BarcodeDataResultAssertions {
    fun isNotFound() = hasFound(BarcodeDataResult.NotFound)
    fun hasFound(expected: Uri) = hasFound(BarcodeDataResult.Found(expected))
    fun hasFound(data: BarcodeDataResult): Matcher<BarcodeDataResult> = equalTo(data)
}
