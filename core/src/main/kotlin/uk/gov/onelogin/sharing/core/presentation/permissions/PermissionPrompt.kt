@file:OptIn(ExperimentalPermissionsApi::class)

package uk.gov.onelogin.sharing.core.presentation.permissions

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import uk.gov.onelogin.sharing.core.presentation.buttons.PermanentPermissionDenialButton
import uk.gov.onelogin.sharing.core.presentation.buttons.PermissionRationaleButton
import uk.gov.onelogin.sharing.core.presentation.buttons.RequirePermissionButton

@Suppress("LongMethod", "ComposableLambdaParameterNaming")
@Composable
fun PermissionPrompt(
    multiplePermissionsState: MultiplePermissionsState,
    hasPreviouslyRequestedPermission: Boolean,
    permanentlyDeniedText: String,
    enablePermissionText: String,
    openSettingsText: String,
    deniedText: String,
    modifier: Modifier = Modifier,
    onGrantedPermissions: @Composable () -> Unit
) {
    MultiplePermissionsScreen(
        state = multiplePermissionsState,
        hasPreviouslyRequestedPermission = hasPreviouslyRequestedPermission,
        onGrantedPermissions = onGrantedPermissions,
        onPermanentlyDenyPermission = {
            PermanentPermissionDenialButton(
                context = LocalContext.current,
                modifier = modifier,
                titleText = permanentlyDeniedText,
                buttonText = openSettingsText
            )
        },
        onRequirePermission = { _, launchPermissions ->
            RequirePermissionButton(
                text = enablePermissionText,
                launchPermission = launchPermissions
            )
        },
        onShowRationale = { _, launchPermissions ->
            PermissionRationaleButton(
                text = enablePermissionText,
                titleText = deniedText,
                launchPermission = launchPermissions
            )
        }
    )
}
