package uk.gov.onelogin.sharing.verifier.scan

import android.net.Uri
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher

object BarcodeDataResultAssertions {
    fun hasNotFoundBarcode() = hasFoundBarcodeData(BarcodeDataResult.NotFound)
    fun hasFoundBarcode(expected: Uri) = hasFoundBarcodeData(BarcodeDataResult.Found(expected))
    fun hasFoundBarcodeData(data: BarcodeDataResult): Matcher<BarcodeDataResult> = equalTo(data)
}
