package uk.gov.onelogin.orchestration

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult
import uk.gov.onelogin.sharing.cameraService.scan.ScanController

@ContributesBinding(ViewModelScope::class, binding = binding<ScanController>())
@Inject
class ScanControllerImpl(private val orchestrator: Orchestrator.Verifier) : ScanController {

    override fun onScanResult(result: BarcodeDataResult) {
        orchestrator.processQrCode(result)
    }

    override fun reset() {
        orchestrator.cancel()
    }
}
