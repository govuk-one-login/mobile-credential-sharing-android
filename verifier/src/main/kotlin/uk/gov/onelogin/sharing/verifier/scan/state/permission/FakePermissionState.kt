package uk.gov.onelogin.sharing.verifier.scan.state.permission

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus

/**
 * Fake implementation of the accompanist [com.google.accompanist.permissions.PermissionState]
 * interface.
 *
 * Kept within production code due to it's use within composable Preview functions.
 *
 * @sample uk.gov.onelogin.sharing.verifier.scan.VerifierScannerPreviewParameters
 */
@OptIn(ExperimentalPermissionsApi::class)
data class FakePermissionState(
    override val permission: String,
    override val status: PermissionStatus,
    private val onLaunchPermission: () -> Unit = {}
) : PermissionState {
    override fun launchPermissionRequest() = onLaunchPermission()
}
