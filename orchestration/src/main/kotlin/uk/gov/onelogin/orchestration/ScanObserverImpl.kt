package uk.gov.onelogin.orchestration

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult
import uk.gov.onelogin.sharing.cameraService.scan.ScanObserver

@ContributesBinding(AppScope::class)
class ScanObserverImpl(
    private val orchestrator: Orchestrator.Verifier
) : ScanObserver {

    override fun onScanResult(result: BarcodeDataResult) {
        orchestrator.processQrCode(result)
    }
}
