package uk.gov.onelogin.sharing.cameraService.scan

import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.barcode.common.Barcode
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeScanResult

/**
 * [BarcodeScanResult.Callback] implementation that defers to the [onQrDetected] lambda when
 * finding an applicable [Uri].
 */
class QrScannerCallback(private val onQrDetected: (String?) -> Unit) :
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
        val qr = when (result) {
            is BarcodeScanResult.Success -> {
                println("scanner callback ${result.first().url}")
                result.first()
            }

            is BarcodeScanResult.Single -> {
                println("scanner callback ${result.barcode.url}")
                result.barcode
            }

            else -> null
        }?.let { barcode ->
            when (barcode.valueType) {
                Barcode.TYPE_URL -> barcode.url?.url
                else -> barcode.rawValue
            }
        }

        if (qr == null) toggleScanner()
        onQrDetected(qr)
    }
}
