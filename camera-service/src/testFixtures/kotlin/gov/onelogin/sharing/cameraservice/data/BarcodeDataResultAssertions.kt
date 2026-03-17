package gov.onelogin.sharing.cameraservice.data

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult

/**
 * Wrapper object for [Matcher] assertions against a [BarcodeDataResult] object.
 */
object BarcodeDataResultAssertions {
    fun isNotFound() = hasFound(BarcodeDataResult.NotFound)
    fun hasFound(expected: String) = hasFound(BarcodeDataResult.Valid(expected))
    fun hasFound(data: BarcodeDataResult): Matcher<BarcodeDataResult> = equalTo(data)
}
