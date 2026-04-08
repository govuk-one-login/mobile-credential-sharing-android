package uk.gov.onelogin.sharing.testapp

import android.Manifest
import android.os.Build
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
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import uk.gov.onelogin.sharing.core.presentation.permissions.PermissionPrompt
import uk.gov.onelogin.sharing.core.presentation.permissions.PermissionPromptText

/**
 * Temporary permission gate for the verifier flow.
 * Creates the [MultiplePermissionsState] and delegates to [VerifierPermissionGateContent].
 *
 * Remove this once the SDK prerequisite screen handles permissions natively.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VerifierPermissionGate(onGrantAll: () -> Unit) {
    var hasPreviouslyRequested by rememberSaveable { mutableStateOf(false) }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = verifierPermissions()
    ) {
        hasPreviouslyRequested = true
    }

    VerifierPermissionGateContent(
        permissionsState = permissionsState,
        hasPreviouslyRequested = hasPreviouslyRequested,
        onGrantAll = onGrantAll
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun VerifierPermissionGateContent(
    permissionsState: MultiplePermissionsState,
    hasPreviouslyRequested: Boolean,
    onGrantAll: () -> Unit
) {
    val currentOnAllGranted by rememberUpdatedState(onGrantAll)

    if (permissionsState.allPermissionsGranted) {
        LaunchedEffect(Unit) { currentOnAllGranted() }
        return
    }

    val cameraGranted = permissionsState.permissions
        .find { it.permission == Manifest.permission.CAMERA }
        ?.status?.isGranted == true

    val bluetoothGranted = permissionsState.permissions
        .filter { it.permission != Manifest.permission.CAMERA }
        .all { it.status.isGranted }

    val text = when {
        !cameraGranted && !bluetoothGranted -> PermissionPromptText(
            permanentlyDeniedText = stringResource(R.string.permissions_permanently_denied),
            enablePermissionText = stringResource(R.string.enable_permissions),
            openSettingsText = stringResource(R.string.open_app_permissions),
            deniedText = stringResource(R.string.permissions_denied)
        )

        !cameraGranted -> PermissionPromptText(
            permanentlyDeniedText = stringResource(R.string.camera_permission_permanently_denied),
            enablePermissionText = stringResource(R.string.enable_camera_permission),
            openSettingsText = stringResource(R.string.open_app_permissions),
            deniedText = stringResource(R.string.camera_permission_denied)
        )

        else -> PermissionPromptText(
            permanentlyDeniedText = stringResource(
                R.string.bluetooth_permission_permanently_denied
            ),
            enablePermissionText = stringResource(R.string.enable_bluetooth_permission),
            openSettingsText = stringResource(R.string.open_app_permissions),
            deniedText = stringResource(R.string.bluetooth_permission_denied)
        )
    }

    PermissionPrompt(
        multiplePermissionsState = permissionsState,
        hasPreviouslyRequestedPermission = hasPreviouslyRequested,
        text = text
    ) {}
}

private fun verifierPermissions(): List<String> = buildList {
    add(Manifest.permission.CAMERA)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        add(Manifest.permission.BLUETOOTH_CONNECT)
        add(Manifest.permission.BLUETOOTH_ADVERTISE)
        add(Manifest.permission.BLUETOOTH_SCAN)
    } else {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.BLUETOOTH)
    }
}
