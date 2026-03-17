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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import uk.gov.android.ui.componentsv2.camera.CameraContentViewModel
import uk.gov.android.ui.componentsv2.camera.ImageProxyConverter
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeScanResult
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeUseCaseProviders
import uk.gov.android.ui.componentsv2.camera.qr.CentrallyCroppedImageProxyConverter
import uk.gov.android.ui.patterns.camera.qr.ModifierExtensions
import uk.gov.android.ui.patterns.camera.qr.QrScannerScreen
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.m3.QrScannerOverlayDefaults
import uk.gov.android.ui.theme.spacingDouble

@Composable
fun VerifierScannerContent(
    lifecycleOwner: LifecycleOwner,
    barcodeScanResultCallback: BarcodeScanResult.Callback,
    modifier: Modifier = Modifier,
    viewModel: CameraContentViewModel = viewModel<CameraContentViewModel>()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.resetState()
        val analysisUseCase = verifierScannerBarcodeAnalysis(
            context = context,
            getCurrentCamera = viewModel::getCurrentCamera,
            converter = CentrallyCroppedImageProxyConverter(),
            callback = barcodeScanResultCallback
        )
        viewModel.update(analysisUseCase)
    }

    val coroutineScope: CoroutineScope = rememberCoroutineScope()
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
}

@Composable
@androidx.compose.ui.tooling.preview.Preview
internal fun VerifierScannerContentPreview() {
    GdsTheme {
        Column(
            modifier = Modifier
                .background(GdsLocalColorScheme.current.rowBackground)
                .padding(spacingDouble)
        ) {
            VerifierScannerContent(
                lifecycleOwner = LocalLifecycleOwner.current,
                modifier = Modifier.testTag("preview"),
                barcodeScanResultCallback = { _, _ -> }
            )
        }
    }
}

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
