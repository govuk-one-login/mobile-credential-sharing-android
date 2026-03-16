package gov.onelogin.sharing.cameraservice.scan

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult
import uk.gov.onelogin.sharing.cameraService.scan.ScanController

class FakeScanController : ScanController {

    private val _lastResult = MutableStateFlow<BarcodeDataResult>(BarcodeDataResult.NotFound)

    val lastResult: StateFlow<BarcodeDataResult> = _lastResult.asStateFlow()

    var resetCalled: Boolean = false
        private set

    override fun onScanResult(result: BarcodeDataResult) {
        _lastResult.value = result
    }

    override fun reset() {
        resetCalled = true
        _lastResult.value = BarcodeDataResult.NotFound
    }
}
