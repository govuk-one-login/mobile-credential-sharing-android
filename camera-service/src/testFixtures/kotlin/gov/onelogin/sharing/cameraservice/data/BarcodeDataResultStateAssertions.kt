package gov.onelogin.sharing.cameraservice.data

import gov.onelogin.sharing.cameraservice.data.BarcodeDataResultAssertions.hasFound
import gov.onelogin.sharing.cameraservice.data.BarcodeDataResultAssertions.isNotFound
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResultState
import uk.gov.onelogin.sharing.verifier.scan.state.data.HasBarcodeScanData

/**
 * Wrapper object containing [Matcher] functions that map to [BarcodeDataResultState.State] objects.
 */
object BarcodeDataResultStateAssertions {
    fun hasNoBarcodeData() = hasBarcodeData(isNotFound())
    fun hasBarcodeData(expected: String) = hasBarcodeData(hasFound(expected))
    fun hasBarcodeData(matcher: Matcher<BarcodeDataResult>): Matcher<BarcodeDataResultState.State> =
        HasBarcodeScanData(matcher)
}
