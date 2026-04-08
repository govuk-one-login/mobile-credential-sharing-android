package uk.gov.onelogin.sharing.testapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker.Companion.bluetoothPermissions
import uk.gov.onelogin.sharing.core.presentation.permissions.PermissionPrompt
import uk.gov.onelogin.sharing.core.presentation.permissions.PermissionPromptText

/**
 * Temporary permission gate for the holder flow.
 * Creates the [MultiplePermissionsState] and delegates to [HolderPermissionGateContent].
 *
 * Remove this once the SDK prerequisite screen handles permissions natively.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HolderPermissionGate(onGrantAll: () -> Unit) {
    var hasPreviouslyRequested by rememberSaveable { mutableStateOf(false) }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = bluetoothPermissions()
    ) {
        hasPreviouslyRequested = true
    }

    HolderPermissionGateContent(
        permissionsState = permissionsState,
        hasPreviouslyRequested = hasPreviouslyRequested,
        onGrantAll = onGrantAll
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun HolderPermissionGateContent(
    permissionsState: MultiplePermissionsState,
    hasPreviouslyRequested: Boolean,
    onGrantAll: () -> Unit
) {
    val currentOnAllGranted by rememberUpdatedState(onGrantAll)

    if (permissionsState.allPermissionsGranted) {
        LaunchedEffect(Unit) { currentOnAllGranted() }
        return
    }

    PermissionPrompt(
        multiplePermissionsState = permissionsState,
        hasPreviouslyRequestedPermission = hasPreviouslyRequested,
        text = PermissionPromptText(
            permanentlyDeniedText = stringResource(
                R.string.bluetooth_permission_permanently_denied
            ),
            enablePermissionText = stringResource(R.string.enable_bluetooth_permission),
            openSettingsText = stringResource(R.string.open_app_permissions),
            deniedText = stringResource(R.string.bluetooth_permission_denied)
        )
    ) {}
}
