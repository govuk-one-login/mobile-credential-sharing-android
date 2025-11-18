package uk.gov.onelogin.sharing.verifier.scan

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.camera.core.Camera
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
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
import androidx.compose.ui.tooling.preview.Preview as ComposablePreview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
import uk.gov.onelogin.sharing.models.dev.ImplementationDetail
import uk.gov.onelogin.sharing.models.dev.RequiresImplementation
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
    val launcher = rememberLauncherForActivityResult(
        BarcodeAnalysisUrlContract()
    ) {
        viewModel.resetUri()
    }

    val uri by viewModel.uri.collectAsStateWithLifecycle()

    if (uri != null) {
        launcher.launch(uri!!)
    } else {
        VerifierScanner(
            lifecycleOwner = lifecycleOwner,
            onUpdatePreviouslyDeniedPermission = viewModel::update,
            hasPreviouslyDeniedPermission = hasPreviouslyDeniedPermission,
            permissionState = permissionState,
            modifier = modifier,
            onUpdateUri = viewModel::update
        )
    }
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun VerifierScanner(
    lifecycleOwner: LifecycleOwner,
    onUpdatePreviouslyDeniedPermission: (Boolean) -> Unit,
    hasPreviouslyDeniedPermission: Boolean,
    permissionState: PermissionState,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    viewModel: CameraContentViewModel = viewModel<CameraContentViewModel>(),
    onUpdateUri: (Uri) -> Unit = {}
) {
    val latestUpdatePreviouslyDeniedPermission by
        rememberUpdatedState(onUpdatePreviouslyDeniedPermission)

    viewModel.resetState()
    verifierScannerBarcodeAnalysis(
        context = LocalContext.current,
        getCurrentCamera = viewModel::getCurrentCamera,
        converter = CentrallyCroppedImageProxyConverter(),
        onUrlFound = { uri ->
            coroutineScope.launch {
                onUpdateUri(uri)
            }
        }
    ).let(viewModel::update)

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
        logic = verifierScannerPermissionLogic(
            context = LocalContext.current,
            modifier = modifier
        ),
        permissionState = permissionState
    )
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
        val imageCaptureUseCase: ImageCapture? by
            viewModel.imageCapture.collectAsStateWithLifecycle(
                initialValue = null,
                lifecycleOwner = lifecycleOwner
            )

        QrScannerScreen(
            modifier = modifier,
            surfaceRequest = surfaceRequest,
            previewUseCase = previewUseCase,
            analysisUseCase = analysisUseCase,
            imageCaptureUseCase = imageCaptureUseCase,
            scanningWidthMultiplier = ModifierExtensions.CANVAS_WIDTH_MULTIPLIER,
            coroutineScope = coroutineScope,
            onUpdateViewModelCamera = viewModel::update,
            colors = QrScannerOverlayDefaults
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
@ComposablePreview
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

private fun verifierScannerBarcodeAnalysis(
    context: Context,
    getCurrentCamera: () -> Camera?,
    converter: ImageProxyConverter,
    onUrlFound: (Uri) -> Unit
) = BarcodeUseCaseProviders.barcodeAnalysis(
    context = context,
    options =
    BarcodeUseCaseProviders.provideQrScanningOptions(
        BarcodeUseCaseProviders.provideZoomOptions(getCurrentCamera)
    ),
    callback = qrScannerDemoCallback(
        onUrlFound = onUrlFound
    ),
    converter = converter
)

@RequiresImplementation(
    details = [
        ImplementationDetail(
            ticket = "DCMAW-16278",
            description = "Invalid QR error handling"
        )
    ]
)
private fun qrScannerDemoCallback(onUrlFound: (Uri) -> Unit): BarcodeScanResult.Callback =
    BarcodeScanResult.Callback { result, toggleScanner ->
        val logTag = "QrScannerScreenDemo"
        if (Log.isLoggable(logTag, Log.INFO)) {
            Log.i(logTag, "Obtained BarcodeScanResult: $result")
        }
        when (result) {
            is BarcodeScanResult.Success -> result.firstOrNull()?.url?.url
            is BarcodeScanResult.Single -> result.barcode.url?.url
            else -> {
                null
            }
        }?.let { url ->
            onUrlFound(url.toUri())
            toggleScanner()
        } ?: toggleScanner()
    }
