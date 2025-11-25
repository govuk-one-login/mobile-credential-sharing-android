package uk.gov.onelogin.sharing.verifier.scan.state.data

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

sealed interface BarcodeDataResultState {
    /**
     * Combines functionality of the [State] and [Updater] interfaces.
     *
     * @see State
     * @see Updater
     */
    interface Complete :
        State,
        Updater {
        companion object {
            @JvmStatic
            fun from(flow: MutableStateFlow<BarcodeDataResult>) = object : Complete {
                override val barcodeDataResult: StateFlow<BarcodeDataResult>
                    get() = flow

                override fun update(result: BarcodeDataResult) {
                    flow.update { result }
                }
            }
        }
    }

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
        fun update(uri: String) = update(BarcodeDataResult.Valid(uri))
    }
}
