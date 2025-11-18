package uk.gov.onelogin.sharing.verifier.scan

import android.Manifest
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import uk.gov.android.ui.componentsv2.permission.PermissionLogic
import uk.gov.android.ui.componentsv2.permission.PermissionScreen
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.onelogin.sharing.models.dev.ImplementationDetail
import uk.gov.onelogin.sharing.models.dev.RequiresImplementation
import uk.gov.onelogin.sharing.verifier.R
import uk.gov.onelogin.sharing.verifier.scan.buttons.CameraPermissionRationaleButton
import uk.gov.onelogin.sharing.verifier.scan.buttons.CameraRequirePermissionButton
import uk.gov.onelogin.sharing.verifier.scan.buttons.PermanentCameraDenial

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VerifierScanner(
    modifier: Modifier = Modifier,
    viewModel: VerifierScannerViewModel = viewModel<VerifierScannerViewModel>(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    permissionState: PermissionState = rememberPermissionState(Manifest.permission.CAMERA) {
        viewModel.update(!it)
    }
) {
    val hasPreviouslyDeniedPermission: Boolean by viewModel
        .hasPreviouslyDeniedPermission
        .collectAsStateWithLifecycle()

    VerifierScanner(
        lifecycleOwner = lifecycleOwner,
        onUpdatePreviouslyDeniedPermission = viewModel::update,
        hasPreviouslyDeniedPermission = hasPreviouslyDeniedPermission,
        permissionState = permissionState,
        modifier = modifier
    )
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun VerifierScanner(
    lifecycleOwner: LifecycleOwner,
    onUpdatePreviouslyDeniedPermission: (Boolean) -> Unit,
    hasPreviouslyDeniedPermission: Boolean,
    permissionState: PermissionState,
    modifier: Modifier = Modifier
) {
    val latestUpdatePreviouslyDeniedPermission by
        rememberUpdatedState(onUpdatePreviouslyDeniedPermission)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                // Allow a User to re-request permissions after navigating away via permanent
                // denial.
                latestUpdatePreviouslyDeniedPermission(false)
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
        PermanentCameraDenial(context, modifier)
    },
    onShowRationale = @RequiresImplementation(
        details = [
            ImplementationDetail(
                ticket = "DCMAW-16275",
                description = "Finalise UI for camera permission rationale"
            )
        ]
    ) { _, launchPermission ->
        CameraPermissionRationaleButton(
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
        CameraRequirePermissionButton(
            launchPermission = launchPermission,
            modifier = modifier
        )
    }
)

@Composable
@OptIn(ExperimentalPermissionsApi::class)
@Preview
internal fun VerifierScannerPreview(
    @PreviewParameter(VerifierScannerPreviewParameters::class)
    permissionStates: Pair<PermissionState, Boolean>
) {
    GdsTheme {
        Column(
            modifier = Modifier
                .background(GdsLocalColorScheme.current.listBackground)
                .padding(spacingDouble)
        ) {
            VerifierScanner(
                lifecycleOwner = LocalLifecycleOwner.current,
                onUpdatePreviouslyDeniedPermission = {},
                hasPreviouslyDeniedPermission = permissionStates.second,
                permissionState = permissionStates.first,
                modifier = Modifier.testTag("preview")
            )
        }
    }
}
