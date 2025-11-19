package uk.gov.onelogin.sharing.verifier.scan.state

import kotlinx.coroutines.flow.MutableStateFlow
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultState
import uk.gov.onelogin.sharing.verifier.scan.state.data.MutableBarcodeDataResultState
import uk.gov.onelogin.sharing.verifier.scan.state.permission.MutablePreviouslyDeniedPermissionState
import uk.gov.onelogin.sharing.verifier.scan.state.permission.PreviouslyDeniedPermissionState

/**
 * [VerifierScannerState.Complete] implementation that relies upon interface delegation.
 *
 * By default, all constructor parameters are implementations backed by [MutableStateFlow] objects.
 */
class CompleteVerifierScannerState(
    barcodeDataResultState: BarcodeDataResultState.Complete = MutableBarcodeDataResultState(),
    previouslyDeniedPermissionState: PreviouslyDeniedPermissionState.Complete =
        MutablePreviouslyDeniedPermissionState()
) : BarcodeDataResultState.Complete by barcodeDataResultState,
    PreviouslyDeniedPermissionState.Complete by previouslyDeniedPermissionState,
    VerifierScannerState.Complete
