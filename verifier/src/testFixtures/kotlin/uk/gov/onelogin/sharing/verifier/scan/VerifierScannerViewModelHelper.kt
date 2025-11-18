package uk.gov.onelogin.sharing.verifier.scan

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope

object VerifierScannerViewModelHelper {
    fun TestScope.monitor(viewModel: VerifierScannerViewModel) {
        listOf(
            viewModel.hasPreviouslyDeniedPermission
        ).forEach {
            backgroundScope.launch { it.collect {} }
        }
    }
}
