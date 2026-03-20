package uk.gov.onelogin.sharing.cameraService.state

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.MutableStateFlow
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResultState
import uk.gov.onelogin.sharing.cameraService.data.MutableBarcodeDataResultState
import uk.gov.onelogin.sharing.core.VerifierUiScope

/**
 * [ScannerState.Complete] implementation that relies upon interface delegation.
 *
 * By default, all constructor parameters are implementations backed by [MutableStateFlow] objects.
 */
@Inject
@ContributesBinding(VerifierUiScope::class, binding = binding<ScannerState.Complete>())
@SingleIn(VerifierUiScope::class)
class CompleteScannerState(
    barcodeDataResultState: BarcodeDataResultState.Complete = MutableBarcodeDataResultState()
) : BarcodeDataResultState.Complete by barcodeDataResultState,
    ScannerState.Complete
