package uk.gov.onelogin.sharing.verifier.scan.state.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * [BarcodeDataResultState.Complete] implementation that defers to the internal [state].
 */
data class MutableBarcodeDataResultState(
    private val state: MutableStateFlow<BarcodeDataResult> =
        MutableStateFlow(BarcodeDataResult.NotFound)
) : BarcodeDataResultState.Complete {
    override val barcodeDataResult: StateFlow<BarcodeDataResult> = state
    override fun update(result: BarcodeDataResult) {
        state.update { result }
    }
}
