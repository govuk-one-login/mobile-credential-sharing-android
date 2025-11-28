package uk.gov.onelogin.sharing.verifier.scan.callbacks

import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.barcode.common.Barcode
import kotlin.let
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeScanResult
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.core.implementation.RequiresImplementation
import uk.gov.onelogin.sharing.security.engagement.Engagement
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResult

/**
 * [BarcodeScanResult.Callback] implementation that defers to the [onDataFound] parameter when
 * finding an applicable [Uri].
 */
@RequiresImplementation(
    details = [
        ImplementationDetail(
            ticket = "DCMAW-16278",
            description = "Invalid QR error handling"
        )
    ]
)
class VerifierScannerBarcodeScanCallback(
    private val onDataFound: (BarcodeDataResult) -> Unit = {}
) : BarcodeScanResult.Callback {

    override fun onResult(result: BarcodeScanResult, toggleScanner: () -> Unit) {
        if (
            Log.isLoggable(
                this::class.java.simpleName,
                Log.INFO
            )
        ) {
            Log.i(
                this::class.java.simpleName,
                "Obtained BarcodeScanResult: $result"
            )
        }
        val result = when (result) {
            is BarcodeScanResult.Success -> result.first()
            is BarcodeScanResult.Single -> result.barcode
            else -> null
        }?.let { barcode ->
            when (barcode.valueType) {
                Barcode.TYPE_URL -> barcode.url?.url
                else -> barcode.rawValue
            }
        }?.let { url ->
            if (url.startsWith(Engagement.QR_CODE_SCHEME)) {
                BarcodeDataResult.Valid(url.removePrefix(Engagement.QR_CODE_SCHEME))
            } else {
                BarcodeDataResult.Invalid(url)
            }
        } ?: BarcodeDataResult.NotFound

        onDataFound(result)
        toggleScanner()
    }
}
