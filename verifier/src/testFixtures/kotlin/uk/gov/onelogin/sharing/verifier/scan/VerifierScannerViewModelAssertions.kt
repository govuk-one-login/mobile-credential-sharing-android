package uk.gov.onelogin.sharing.verifier.scan

import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStateAssertions.hasNoBarcodeData
import uk.gov.onelogin.sharing.verifier.scan.state.permission.PreviouslyDeniedPermissionStateAssertions.hasPreviouslyGrantedPermission

object VerifierScannerViewModelAssertions {
    fun isInInitialState(): Matcher<VerifierScannerViewModel> = allOf(
        hasPreviouslyGrantedPermission(),
        hasNoBarcodeData()
    )
}
