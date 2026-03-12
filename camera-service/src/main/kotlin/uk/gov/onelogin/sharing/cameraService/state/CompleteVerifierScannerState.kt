package uk.gov.onelogin.sharing.cameraService.state

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResultState
import uk.gov.onelogin.sharing.cameraService.data.MutableBarcodeDataResultState

/**
 * [ScannerState.Complete] implementation that relies upon interface delegation.
 *
 * By default, all constructor parameters are implementations backed by [MutableStateFlow] objects.
 */
@Inject
@ContributesBinding(ViewModelScope::class, binding = binding<ScannerState.Complete>())
class CompleteVerifierScannerState(
    barcodeDataResultState: BarcodeDataResultState.Complete = MutableBarcodeDataResultState()
) : BarcodeDataResultState.Complete by barcodeDataResultState,
    ScannerState.Complete
