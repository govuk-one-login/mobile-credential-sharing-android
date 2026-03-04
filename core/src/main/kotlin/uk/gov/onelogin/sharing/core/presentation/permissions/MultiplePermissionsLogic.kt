package uk.gov.onelogin.sharing.core.presentation.permissions

import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
data class MultiplePermissionsLogic(
    val onGrantPermission: @Composable () -> Unit = {},
    val onPermanentlyDenyPermission: @Composable (
        permissionState: MultiplePermissionsState
    ) -> Unit = { _ -> },
    val onRequirePermission: @Composable (
        permissionState: MultiplePermissionsState,
        launchPermission: () -> Unit
    ) -> Unit = { _, _ -> },
    val onShowRationale: @Composable (
        permissionState: MultiplePermissionsState,
        launchPermission: () -> Unit
    ) -> Unit = { _, _ -> }
)
