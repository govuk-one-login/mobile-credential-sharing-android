@file:OptIn(ExperimentalPermissionsApi::class)

package uk.gov.onelogin.sharing.bluetooth.permissions

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import uk.gov.onelogin.sharing.core.presentation.buttons.PermanentPermissionDenialButton
import uk.gov.onelogin.sharing.core.presentation.buttons.PermissionRationaleButton
import uk.gov.onelogin.sharing.core.presentation.buttons.RequirePermissionButton
import uk.gov.onelogin.sharing.core.presentation.permissions.MultiplePermissionsScreen

@Suppress("LongMethod", "ComposableLambdaParameterNaming")
@Composable
fun BluetoothPermissionPrompt(
    multiplePermissionsState: MultiplePermissionsState,
    hasPreviouslyRequestedPermission: Boolean,
    modifier: Modifier = Modifier,
    onGrantedPermissions: @Composable () -> Unit
) {
    MultiplePermissionsScreen(
        state = multiplePermissionsState,
        hasPreviouslyRequestedPermission = hasPreviouslyRequestedPermission,
        onGrantedPermissions = onGrantedPermissions,
        onPermanentlyDenyPermission = { _ ->
            PermanentPermissionDenialButton(
                context = LocalContext.current,
                modifier = modifier,
                titleText = "Permanently denied open in your settings",
                buttonText = "Open settings"
            )
        },
        onRequirePermission = { _, launchPermissions ->
            RequirePermissionButton(
                text = "Enable bluetooth permissions",
                launchPermission = launchPermissions
            )
        },
        onShowRationale = { _, launchPermissions ->
            PermissionRationaleButton(
                text = "Enable bluetooth permissions",
                launchPermission = launchPermissions
            )
        }
    )
}
