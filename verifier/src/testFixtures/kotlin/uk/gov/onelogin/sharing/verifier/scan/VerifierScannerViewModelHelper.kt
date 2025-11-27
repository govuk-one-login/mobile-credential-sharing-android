package uk.gov.onelogin.sharing.verifier.scan

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope

/**
 * Wrapper object for containing functions that help test the [VerifierScannerViewModel].
 */
object VerifierScannerViewModelHelper {
    /**
     * Launches [kotlinx.coroutines.flow.StateFlow] collection for the provided [model] within
     * background coroutines.
     *
     * This ensures that state flows return the latest value.
     *
     * See also:
     * - [Android developer documentation: Testing StateFlow](https://developer.android.com/kotlin/flow/test#stateflows)
     */
    fun TestScope.monitor(model: VerifierScannerViewModel) {
        listOf(
            model.hasPreviouslyDeniedPermission,
            model.barcodeDataResult
        ).forEach {
            backgroundScope.launch { it.collect {} }
        }
    }
}
