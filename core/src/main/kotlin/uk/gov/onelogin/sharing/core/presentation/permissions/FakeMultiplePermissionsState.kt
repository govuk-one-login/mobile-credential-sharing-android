package uk.gov.onelogin.sharing.core.presentation.permissions

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Fake implementation of the accompanist [PermissionState] interface.
 *
 * Kept within production code due to it's use within composable Preview functions.
 *
 */

@OptIn(ExperimentalPermissionsApi::class)
class FakeMultiplePermissionsState(
    override val permissions: List<PermissionState>,
    private val onLaunchPermission: () -> Unit
) : MultiplePermissionsState {

    override val allPermissionsGranted: Boolean
        get() = permissions.all { it.status.isGranted }

    override val revokedPermissions: List<PermissionState>
        get() = permissions.filter { !it.status.isGranted }

    override val shouldShowRationale: Boolean
        get() = permissions.any { it.status.shouldShowRationale }

    override fun launchMultiplePermissionRequest() = onLaunchPermission()
}