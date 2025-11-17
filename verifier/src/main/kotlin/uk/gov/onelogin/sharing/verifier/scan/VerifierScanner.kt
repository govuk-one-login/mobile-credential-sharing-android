package uk.gov.onelogin.sharing.verifier.scan

import android.Manifest
import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import uk.gov.android.ui.componentsv2.permission.PermissionLogic
import uk.gov.android.ui.componentsv2.permission.PermissionScreen
import uk.gov.onelogin.sharing.models.dev.ImplementationDetail
import uk.gov.onelogin.sharing.models.dev.RequiresImplementation
import uk.gov.onelogin.sharing.verifier.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VerifierScanner(
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val (
        hasPreviouslyDeniedPermission,
        onUpdatePreviouslyDeniedPermission
    ) = rememberSaveable { mutableStateOf(false) }

    val permissionState =
        rememberPermissionState(Manifest.permission.CAMERA) {
            onUpdatePreviouslyDeniedPermission(!it)
        }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                // Allow a User to re-request permissions after navigating away via permanent
                // denial.
                onUpdatePreviouslyDeniedPermission(false)
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    PermissionScreen(
        hasPreviouslyDeniedPermission = hasPreviouslyDeniedPermission,
        logic = verifierScannerPermissionLogic(context = LocalContext.current, modifier = modifier),
        permissionState = permissionState
    )
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun verifierScannerPermissionLogic(
    context: Context,
    modifier: Modifier = Modifier
): PermissionLogic = PermissionLogic(

    onGrantPermission = @RequiresImplementation(
        details = [
            ImplementationDetail(
                ticket = "DCMAW-16276",
                description = "QR Scanner Screen UI"
            ),
            ImplementationDetail(
                ticket = "DCMAW-16278",
                description = "Invalid QR error handling"
            )
        ]
    ) {
        Text(
            stringResource(
                R.string.verifier_scanner_camera_permission_enabled
            ),
            modifier = modifier
        )
    },
    onPermissionPermanentlyDenied = @RequiresImplementation(
        details = [
            ImplementationDetail(
                ticket = "DCMAW-16275",
                description = "Finalise UI for permanent camera permission denial"
            )
        ]
    ) { _ ->
        VerifierScannerPermissionButtons.PermanentCameraDenial(context, modifier)
    },
    onShowRationale = @RequiresImplementation(
        details = [
            ImplementationDetail(
                ticket = "DCMAW-16275",
                description = "Finalise UI for camera permission rationale"
            )
        ]
    ) { _, launchPermission ->
        VerifierScannerPermissionButtons.CameraPermissionRationaleButton(
            launchPermission = launchPermission,
            modifier = modifier
        )
    },
    onRequirePermission = @RequiresImplementation(
        details = [
            ImplementationDetail(
                ticket = "DCMAW-16275",
                description = "Finalise UI for camera permission denial"
            )
        ]
    ) { _, launchPermission ->
        VerifierScannerPermissionButtons.CameraRequirePermissionButton(
            launchPermission = launchPermission,
            modifier = modifier
        )
    }
)
