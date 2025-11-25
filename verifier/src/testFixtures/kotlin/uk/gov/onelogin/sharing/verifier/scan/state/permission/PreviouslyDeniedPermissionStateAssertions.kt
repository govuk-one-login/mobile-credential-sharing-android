package uk.gov.onelogin.sharing.verifier.scan.state.permission

import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerViewModel

/**
 * Wrapper object containing [Matcher] assertions for [PreviouslyDeniedPermissionState.State]
 * objects.
 */
object PreviouslyDeniedPermissionStateAssertions {
    /**
     * Verifies that the object has previously denied a permission request.
     */
    fun hasPreviouslyDeniedPermission() = hasPreviouslyDeniedPermission(true)

    /**
     * Verifies that the [PreviouslyDeniedPermissionState.State]'s
     * [kotlinx.coroutines.flow.StateFlow.value] matches the [expected] parameter.
     */
    fun hasPreviouslyDeniedPermission(
        expected: Boolean
    ): Matcher<PreviouslyDeniedPermissionState.State> = HasPreviouslyDeniedPermission(expected)

    /**
     * Verifies that the object had previously granted a permission request.
     */
    fun hasPreviouslyGrantedPermission() = hasPreviouslyDeniedPermission(false)
}
