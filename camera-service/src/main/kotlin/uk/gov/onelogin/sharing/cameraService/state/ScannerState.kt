package uk.gov.onelogin.sharing.cameraService.state

import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResultState

sealed interface ScannerState {

    /**
     * Interface that combines both [States].
     *
     * @sample uk.gov.onelogin.sharing.cameraService.scan.ScannerViewModel
     *
     * @see States
     *
     */
    interface Complete :
        BarcodeDataResultState.Complete,
        States

    /**
     * Property bag Interface that combines all of the StateFlow based Interfaces.
     *
     * @see BarcodeDataResultState.State
     */
    interface States : BarcodeDataResultState.State
}
