package uk.gov.onelogin.sharing.verifier.scan

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeScanResult
import uk.gov.onelogin.sharing.verifier.scan.callbacks.VerifierScannerBarcodeScanCallback
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResult

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
        BarcodeAnalysisUrlContract { _, _ ->
            viewModel.resetBarcodeData()
        }
    ) {
        // do nothing as it's handled within the constructor parameter.
    }

    val barcodeScanResultCallback: BarcodeScanResult.Callback = VerifierScannerBarcodeScanCallback(
        onUrlFound = viewModel::update
    )

    VerifierScannerContent(
        lifecycleOwner = lifecycleOwner,
        onUpdatePreviouslyDeniedPermission = viewModel::update,
        hasPreviouslyDeniedPermission = hasPreviouslyDeniedPermission,
        permissionState = permissionState,
        modifier = modifier,
        barcodeScanResultCallback = barcodeScanResultCallback
    )

    val uri: BarcodeDataResult by viewModel.barcodeDataResult.collectAsStateWithLifecycle()

    LaunchedEffect(uri) {
        when (uri) {
            is BarcodeDataResult.Found -> launcher.launch((uri as BarcodeDataResult.Found).data)

            else -> {
                // do nothing as there's no barcode Uri to launch
            }
        }
    }
}
