package uk.gov.onelogin.sharing.verifier.scan.state.data

import android.net.Uri
import kotlinx.coroutines.flow.StateFlow

sealed interface BarcodeDataResultState {
    /**
     * Combines functionality of the [State] and [Updater] interfaces.
     *
     * @see State
     * @see Updater
     */
    interface Complete :
        State,
        Updater

    /**
     * Interface for exposing a [BarcodeDataResult] [StateFlow]. Commonly paired with the [Updater]
     * interface.
     *
     * @see Updater
     */
    interface State {
        val barcodeDataResult: StateFlow<BarcodeDataResult>
    }

    /**
     * Interface for updating a [BarcodeDataResult] object. Commonly paired with the [State]
     * interface.
     *
     * @see State
     */
    fun interface Updater {
        fun update(result: BarcodeDataResult)
        fun update(uri: Uri) = update(BarcodeDataResult.Found(uri))
    }
}
