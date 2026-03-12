package uk.gov.onelogin.sharing.cameraService.state

import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResultState

sealed interface ScannerState {

    /**
     * Interface that combines both [States] and [Updaters].
     *
     * @sample uk.gov.onelogin.sharing.verifier.scan.VerifierScannerViewModel
     *
     * @see States
     * @see Updaters
     */
    interface Complete :
        BarcodeDataResultState.Complete,
        States,
        Updaters

    /**
     * Property bag Interface that combines all of the StateFlow based Interfaces.
     *
     * @see BarcodeDataResultState.State
     * @see PreviouslyDeniedPermissionState.State
     */
    interface States :
        BarcodeDataResultState.State

    /**
     * Interface that combines all of the StateFlow updater Interfaces.
     *
     * @see BarcodeDataResultState.Updater
     * @see PreviouslyDeniedPermissionState.Updater
     */
    interface Updaters :
        BarcodeDataResultState.Updater
}
