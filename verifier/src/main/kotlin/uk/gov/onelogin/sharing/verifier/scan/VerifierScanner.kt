package uk.gov.onelogin.sharing.verifier.scan

import android.Manifest
import androidx.compose.runtime.Composable
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
    },
    onInvalidBarcode: (String) -> Unit = {},
    onValidBarcode: (String) -> Unit = {}
) {
    val hasPreviouslyDeniedPermission: Boolean by viewModel
        .hasPreviouslyDeniedPermission
        .collectAsStateWithLifecycle()

    val barcodeScanResultCallback: BarcodeScanResult.Callback = VerifierScannerBarcodeScanCallback(
        onDataFound = viewModel::update
    )

    val uri: BarcodeDataResult by viewModel.barcodeDataResult.collectAsStateWithLifecycle()

    when (uri) {
        is BarcodeDataResult.Valid -> {
            onValidBarcode((uri as BarcodeDataResult.Valid).data).also {
                viewModel.resetBarcodeData()
            }
        }

        is BarcodeDataResult.Invalid -> {
            onInvalidBarcode((uri as BarcodeDataResult.Invalid).data).also {
                viewModel.resetBarcodeData()
            }
        }

        else -> {
            VerifierScannerContent(
                lifecycleOwner = lifecycleOwner,
                onUpdatePreviouslyDeniedPermission = viewModel::update,
                hasPreviouslyDeniedPermission = hasPreviouslyDeniedPermission,
                permissionState = permissionState,
                modifier = modifier,
                barcodeScanResultCallback = barcodeScanResultCallback
            )
        }
    }
}
