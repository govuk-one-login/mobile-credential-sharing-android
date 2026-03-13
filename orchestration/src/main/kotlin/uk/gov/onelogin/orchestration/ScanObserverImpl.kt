package uk.gov.onelogin.orchestration

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult
import uk.gov.onelogin.sharing.cameraService.scan.ScanObserver

@ContributesBinding(ViewModelScope::class, binding = binding<ScanObserver>())
@Inject
class ScanObserverImpl(
    private val orchestrator: Orchestrator.Verifier
) : ScanObserver {

    override fun onScanResult(result: BarcodeDataResult) {
        orchestrator.processQrCode(result)
    }
}
