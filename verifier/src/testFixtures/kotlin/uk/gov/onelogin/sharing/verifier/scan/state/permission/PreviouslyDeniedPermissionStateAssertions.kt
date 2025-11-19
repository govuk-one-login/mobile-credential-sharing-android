package uk.gov.onelogin.sharing.verifier.scan.state.permission

import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerViewModel

object PreviouslyDeniedPermissionStateAssertions {
    fun hasPreviouslyDeniedPermission() = hasPreviouslyDeniedPermission(true)
    fun hasPreviouslyDeniedPermission(
        expected: Boolean
    ): Matcher<PreviouslyDeniedPermissionState.State> = HasPreviouslyDeniedPermission(expected)

    fun hasPreviouslyGrantedPermission() = hasPreviouslyDeniedPermission(false)
}
