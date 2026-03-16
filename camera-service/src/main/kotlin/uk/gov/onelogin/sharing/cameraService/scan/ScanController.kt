package uk.gov.onelogin.sharing.cameraService.scan

import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult

@ContributesTo(ViewModelScope::class)
interface ScanController {
    fun onScanResult(result: BarcodeDataResult)

    fun reset()
}
