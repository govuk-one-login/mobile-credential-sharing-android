package uk.gov.onelogin.sharing.core.presentation.permissions

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus

@OptIn(ExperimentalPermissionsApi::class)
object FakePermissionStateExt {
    fun String.toFakePermissionState(
        status: PermissionStatus,
        onLaunchPermission: () -> Unit = {}
    ): FakePermissionState = FakePermissionState(
        permission = this,
        status = status,
        onLaunchPermission = onLaunchPermission
    )
}
