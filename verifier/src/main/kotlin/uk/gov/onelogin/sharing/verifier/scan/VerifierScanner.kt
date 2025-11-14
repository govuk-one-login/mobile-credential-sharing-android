package uk.gov.onelogin.sharing.verifier.scan

import android.Manifest
import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import uk.gov.android.ui.componentsv2.permission.PermissionLogic
import uk.gov.android.ui.componentsv2.permission.PermissionScreen
import uk.gov.onelogin.sharing.verifier.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VerifierScanner(modifier: Modifier = Modifier) {
    val (
        hasPreviouslyDeniedPermission,
        onUpdatePreviouslyDeniedPermission
    ) = remember { mutableStateOf(false) }

    val permissionState =
        rememberPermissionState(Manifest.permission.CAMERA) {
            onUpdatePreviouslyDeniedPermission(!it)
        }

    PermissionScreen(
        hasPreviouslyDeniedPermission = hasPreviouslyDeniedPermission,
        logic = verifierScannerPermissionLogic(context = LocalContext.current, modifier = modifier),
        permissionState = permissionState
    )
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
private fun verifierScannerPermissionLogic(
    context: Context,
    modifier: Modifier = Modifier
): PermissionLogic = PermissionLogic(
    onGrantPermission = {
        Text(
            stringResource(
                R.string.verifier_scanner_camera_permission_enabled
            ),
            modifier = modifier
        )
    },
    onPermissionPermanentlyDenied = { _ ->
        VerifierScannerPermissionButtons.PermanentCameraDenial(context, modifier)
    },
    onShowRationale = { _, launchPermission ->
        VerifierScannerPermissionButtons.CameraPermissionRationaleButton(
            launchPermission = launchPermission,
            modifier = modifier
        )
    },
    onRequirePermission = { _, launchPermission ->
        VerifierScannerPermissionButtons.CameraRequirePermissionButton(
            launchPermission = launchPermission,
            modifier = modifier
        )
    }
)
