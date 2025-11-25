package uk.gov.onelogin.sharing.verifier.scan.callbacks

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeScanResult
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.core.implementation.RequiresImplementation

/**
 * [BarcodeScanResult.Callback] implementation that defers to the [onUrlFound] parameter when
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
class VerifierScannerBarcodeScanCallback(private val onUrlFound: (Uri) -> Unit = {}) :
    BarcodeScanResult.Callback {

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
        when (result) {
            is BarcodeScanResult.Success -> result.firstOrNull()?.url?.url
            is BarcodeScanResult.Single -> result.barcode.url?.url
            else -> null
        }?.let { url ->
            onUrlFound(url.toUri())
            toggleScanner()
        } ?: toggleScanner()
    }
}
