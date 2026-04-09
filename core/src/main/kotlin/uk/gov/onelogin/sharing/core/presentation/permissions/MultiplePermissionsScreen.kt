package uk.gov.onelogin.sharing.core.presentation.permissions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MultiplePermissionsScreen(
    state: MultiplePermissionsState,
    hasPreviouslyRequestedPermission: Boolean,
    logic: MultiplePermissionsLogic
) = MultiplePermissionsScreen(
    state = state,
    hasPreviouslyRequestedPermission = hasPreviouslyRequestedPermission,
    onGrantedPermissions = logic.onGrantPermission,
    onPermanentlyDenyPermission = logic.onPermanentlyDenyPermission,
    onRequirePermission = logic.onRequirePermission,
    onShowRationale = logic.onShowRationale
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MultiplePermissionsScreen(
    state: MultiplePermissionsState,
    hasPreviouslyRequestedPermission: Boolean,
    modifier: Modifier = Modifier,
    onPermanentlyDenyPermission: @Composable (
        permissionState: MultiplePermissionsState
    ) -> Unit = { _ -> },
    onRequirePermission: @Composable (
        permissionState: MultiplePermissionsState,
        launchPermission: () -> Unit
    ) -> Unit = { _, _ -> },
    onShowRationale: @Composable (
        permissionState: MultiplePermissionsState,
        launchPermission: () -> Unit
    ) -> Unit = { _, _ -> },
    onGrantedPermissions: @Composable () -> Unit = {}
) {
    when {
        state.allPermissionsGranted -> {
            onGrantedPermissions()
        }

        else -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.shouldShowRationale -> {
                    onShowRationale(state) {
                        state.launchMultiplePermissionRequest()
                    }
                }

                hasPreviouslyRequestedPermission && state.isPermanentlyDenied() -> {
                    onPermanentlyDenyPermission(state)
                }

                else -> {
                    onRequirePermission(state) {
                        state.launchMultiplePermissionRequest()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
fun MultiplePermissionsState.isPermanentlyDenied(): Boolean = permissions.any { perm ->
    !perm.status.isGranted &&
        !perm.status.shouldShowRationale
}
