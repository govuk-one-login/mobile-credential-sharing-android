package uk.gov.onelogin.sharing.bluetooth.api.scanner

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult

interface ScannerCallback {
    val onBatchResults: (List<ScanResult>) -> Unit
    val onFailure: (ScannerFailure) -> Unit
    val onResult: (Int, ScanResult) -> Unit

    companion object {
        @JvmStatic
        fun of(
            onBatchResults: (List<ScanResult>) -> Unit = {},
            onFailure: (ScannerFailure) -> Unit = {},
            onResult: (Int, ScanResult) -> Unit = { _, _ -> }
        ) = object : ScannerCallback {
            override val onBatchResults: (List<ScanResult>) -> Unit = onBatchResults
            override val onFailure: (ScannerFailure) -> Unit = onFailure
            override val onResult: (Int, ScanResult) -> Unit = onResult
        }

        internal fun ScannerCallback.toLeScanCallback(): ScanCallback = object : ScanCallback() {
            override fun onBatchScanResults(results: List<ScanResult>) = onBatchResults(results)

            override fun onScanFailed(errorCode: Int) = ScannerFailure.from(errorCode)
                .let(onFailure::invoke)

            override fun onScanResult(callbackType: Int, result: ScanResult) =
                onResult(callbackType, result)
        }
    }
}
