package uk.gov.onelogin.sharing.verifier.scan

import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStateAssertions.hasNoBarcodeData
import uk.gov.onelogin.sharing.verifier.scan.state.permission.PreviouslyDeniedPermissionStateAssertions.hasPreviouslyGrantedPermission

/**
 * Wrapper object for storing [Matcher] assertions for the [VerifierScannerViewModel].
 */
object VerifierScannerViewModelAssertions {
    /**
     * @see hasPreviouslyGrantedPermission
     * @see hasNoBarcodeData
     */
    fun isInInitialState(): Matcher<VerifierScannerViewModel> = allOf(
        hasPreviouslyGrantedPermission(),
        hasNoBarcodeData()
    )
}
