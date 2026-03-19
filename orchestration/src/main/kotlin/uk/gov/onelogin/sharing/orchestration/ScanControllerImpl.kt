package uk.gov.onelogin.sharing.orchestration

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult
import uk.gov.onelogin.sharing.cameraService.scan.ScanController
import uk.gov.onelogin.sharing.core.VerifierUiScope

@ContributesBinding(VerifierUiScope::class, binding = binding<ScanController>())
@Inject
class ScanControllerImpl(private val orchestrator: Orchestrator.Verifier) : ScanController {

    override fun onScanResult(result: BarcodeDataResult) {
        orchestrator.processQrCode(result)
    }

    override fun reset() {
        orchestrator.cancel()
    }
}
