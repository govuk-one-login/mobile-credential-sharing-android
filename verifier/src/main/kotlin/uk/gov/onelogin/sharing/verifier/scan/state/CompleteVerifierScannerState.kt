package uk.gov.onelogin.sharing.verifier.scan.state

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.MutableStateFlow
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResultState
import uk.gov.onelogin.sharing.cameraService.data.MutableBarcodeDataResultState
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.verifier.scan.state.permission.MutablePreviouslyDeniedPermissionState
import uk.gov.onelogin.sharing.verifier.scan.state.permission.PreviouslyDeniedPermissionState

/**
 * [VerifierScannerState.Complete] implementation that relies upon interface delegation.
 *
 * By default, all constructor parameters are implementations backed by [MutableStateFlow] objects.
 */
@Inject
@ContributesBinding(VerifierUiScope::class, binding = binding<VerifierScannerState.Complete>())
class CompleteVerifierScannerState(
    barcodeDataResultState: BarcodeDataResultState.Complete = MutableBarcodeDataResultState(),
    previouslyDeniedPermissionState: PreviouslyDeniedPermissionState.Complete =
        MutablePreviouslyDeniedPermissionState()
) : BarcodeDataResultState.Complete by barcodeDataResultState,
    PreviouslyDeniedPermissionState.Complete by previouslyDeniedPermissionState,
    VerifierScannerState.Complete
