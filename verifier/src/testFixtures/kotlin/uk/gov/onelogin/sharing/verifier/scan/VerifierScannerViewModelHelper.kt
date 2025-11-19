package uk.gov.onelogin.sharing.verifier.scan

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope

object VerifierScannerViewModelHelper {
    fun TestScope.monitor(model: VerifierScannerViewModel) {
        listOf(
            model.hasPreviouslyDeniedPermission,
            model.barcodeDataResult
        ).forEach {
            backgroundScope.launch { it.collect {} }
        }
    }
}
