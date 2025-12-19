package uk.gov.onelogin.sharing.verifier.scan

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview as ComposablePreview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import kotlinx.coroutines.CoroutineScope
import uk.gov.android.ui.componentsv2.camera.CameraContentViewModel
import uk.gov.android.ui.componentsv2.camera.ImageProxyConverter
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeScanResult
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeUseCaseProviders
import uk.gov.android.ui.componentsv2.camera.qr.CentrallyCroppedImageProxyConverter
import uk.gov.android.ui.componentsv2.permission.PermissionLogic
import uk.gov.android.ui.componentsv2.permission.PermissionScreen
import uk.gov.android.ui.patterns.camera.qr.ModifierExtensions
import uk.gov.android.ui.patterns.camera.qr.QrScannerScreen
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.m3.QrScannerOverlayDefaults
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.core.presentation.buttons.PermanentPermissionDenialButton
import uk.gov.onelogin.sharing.core.presentation.buttons.PermissionRationaleButton
import uk.gov.onelogin.sharing.core.presentation.buttons.RequirePermissionButton
import uk.gov.onelogin.sharing.verifier.R

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun VerifierScannerContent(
    lifecycleOwner: LifecycleOwner,
    onUpdatePreviouslyDeniedPermission: (Boolean) -> Unit,
    hasPreviouslyDeniedPermission: Boolean,
    permissionState: PermissionState,
    barcodeScanResultCallback: BarcodeScanResult.Callback,
    modifier: Modifier = Modifier,
    viewModel: CameraContentViewModel = viewModel<CameraContentViewModel>()
) {
    viewModel.resetState()
    verifierScannerBarcodeAnalysis(
        context = LocalContext.current,
        getCurrentCamera = viewModel::getCurrentCamera,
        converter = CentrallyCroppedImageProxyConverter(),
        callback = barcodeScanResultCallback
    ).let(viewModel::update)

    VerifierScannerContentDisposableEffects(lifecycleOwner, onUpdatePreviouslyDeniedPermission)

    PermissionScreen(
        hasPreviouslyDeniedPermission = hasPreviouslyDeniedPermission,
        logic = verifierScannerPermissionLogic(
            context = LocalContext.current,
            modifier = modifier
        ),
        permissionState = permissionState
    )
}

@Composable
private fun VerifierScannerContentDisposableEffects(
    lifecycleOwner: LifecycleOwner,
    onUpdatePreviouslyDeniedPermission: (Boolean) -> Unit
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
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
@ComposablePreview
internal fun VerifierScannerContentPreview(
    @PreviewParameter(VerifierScannerPreviewParameters::class)
    permissionStates: Pair<PermissionState, Boolean>
) {
    GdsTheme {
        Column(
            modifier = Modifier
                .background(GdsLocalColorScheme.current.rowBackground)
                .padding(spacingDouble)
        ) {
            VerifierScannerContent(
                lifecycleOwner = LocalLifecycleOwner.current,
                onUpdatePreviouslyDeniedPermission = {},
                hasPreviouslyDeniedPermission = permissionStates.second,
                permissionState = permissionStates.first,
                modifier = Modifier.testTag("preview"),
                barcodeScanResultCallback = { _, _ -> }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
@ImplementationDetail(
    ticket = "DCMAW-16276",
    description = "QR Scanner Screen UI"
)
fun verifierScannerPermissionLogic(
    context: Context,
    modifier: Modifier = Modifier,
    viewModel: CameraContentViewModel = viewModel<CameraContentViewModel>(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): PermissionLogic = PermissionLogic(
    onGrantPermission = {
        val surfaceRequest: SurfaceRequest? by
            viewModel.surfaceRequest.collectAsStateWithLifecycle(lifecycleOwner = lifecycleOwner)
        val previewUseCase: Preview by viewModel.preview.collectAsStateWithLifecycle(
            lifecycleOwner = lifecycleOwner
        )
        val analysisUseCase: ImageAnalysis? by viewModel.imageAnalysis.collectAsStateWithLifecycle(
            initialValue = null,
            lifecycleOwner = lifecycleOwner
        )

        QrScannerScreen(
            modifier = modifier,
            surfaceRequest = surfaceRequest,
            previewUseCase = previewUseCase,
            analysisUseCase = analysisUseCase,
            scanningWidthMultiplier = ModifierExtensions.CANVAS_WIDTH_MULTIPLIER,
            coroutineScope = coroutineScope,
            onUpdateViewModelCamera = viewModel::update,
            colors = QrScannerOverlayDefaults
        )
    },
    onPermissionPermanentlyDenied = { _ ->
        PermanentPermissionDenialButton(
            context = context,
            modifier = modifier,
            titleText = stringResource(R.string.enable_camera_permission_to_continue),
            buttonText = stringResource(R.string.open_app_permissions)
        )
    },
    onShowRationale = { _, launchPermission ->
        PermissionRationaleButton(
            modifier = modifier,
            text = stringResource(R.string.verifier_scanner_require_camera_permission),
            launchPermission = launchPermission
        )
    },
    onRequirePermission = { _, launchPermission ->
        RequirePermissionButton(
            modifier = modifier,
            text = stringResource(R.string.verifier_scanner_require_camera_permission),
            launchPermission = launchPermission
        )
    }
)

private fun verifierScannerBarcodeAnalysis(
    context: Context,
    getCurrentCamera: () -> Camera?,
    converter: ImageProxyConverter,
    callback: BarcodeScanResult.Callback
) = BarcodeUseCaseProviders.barcodeAnalysis(
    context = context,
    options =
        BarcodeUseCaseProviders.provideQrScanningOptions(
            BarcodeUseCaseProviders.provideZoomOptions(getCurrentCamera)
        ),
    callback = callback,
    converter = converter
)
