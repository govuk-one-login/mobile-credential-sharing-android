@file:OptIn(ExperimentalPermissionsApi::class)

package uk.gov.onelogin.sharing.bluetooth.permissions

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import uk.gov.onelogin.sharing.core.presentation.buttons.PermanentPermissionDenialButton
import uk.gov.onelogin.sharing.core.presentation.buttons.PermissionRationaleButton
import uk.gov.onelogin.sharing.core.presentation.buttons.RequirePermissionButton

@Suppress("LongMethod", "ComposableLambdaParameterNaming")
@Composable
fun BluetoothPermissionPrompt(
    multiplePermissionsState: MultiplePermissionsState,
    hasPreviouslyRequestedPermission: Boolean,
    modifier: Modifier = Modifier,
    onGrantedPermissions: @Composable () -> Unit
) {
    when {
        multiplePermissionsState.allPermissionsGranted -> {
            onGrantedPermissions()
        }

        multiplePermissionsState.shouldShowRationale -> {
            PermissionRationaleButton(
                text = "Enable bluetooth permissions",
                launchPermission = {
                    multiplePermissionsState.launchMultiplePermissionRequest()
                }
            )
        }

        hasPreviouslyRequestedPermission && multiplePermissionsState.isPermanentlyDenied() -> {
            PermanentPermissionDenialButton(
                context = LocalContext.current,
                modifier = modifier,
                titleText = "Permanently denied open in your settings",
                buttonText = "Open settings"
            )
        }

        else -> {
            RequirePermissionButton(
                text = "Enable bluetooth permissions",
                launchPermission = {
                    multiplePermissionsState.launchMultiplePermissionRequest()
                }
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
fun MultiplePermissionsState.isPermanentlyDenied(): Boolean = permissions.any { perm ->
    !perm.status.isGranted &&
            !perm.status.shouldShowRationale
}
