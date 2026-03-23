package uk.gov.onelogin.sharing.orchestration

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

class FakeOrchestrator(
    val initialHolderState: MutableStateFlow<HolderSessionState> = MutableStateFlow(
        HolderSessionState.NotStarted
    ),
    val initialVerifierState: MutableStateFlow<VerifierSessionState> = MutableStateFlow(
        VerifierSessionState.NotStarted
    )

) : Orchestrator.Holder,
    Orchestrator.Verifier,
    Resettable {

    override val holderSessionState: StateFlow<HolderSessionState> = initialHolderState
    override val verifierSessionState: StateFlow<VerifierSessionState> = initialVerifierState

    override fun processQrCode(qrCode: BarcodeDataResult) {
        when (qrCode) {
            is BarcodeDataResult.Valid -> {
                initialVerifierState.value =
                    VerifierSessionState.Connecting
            }

            is BarcodeDataResult.Invalid -> {
                initialVerifierState.value = VerifierSessionState.Complete.Failed(
                    error = SessionError(
                        message = qrCode.data,
                        exception = IllegalArgumentException("Qr Code is an unsupported format")
                    )
                )
            }

            BarcodeDataResult.NotFound -> Unit
        }
    }

    var startCount = 0
    var cancelCount = 0

    override fun start() {
        startCount++
    }

    override fun cancel() {
        cancelCount++
    }

    override fun reset() {
        startCount = 0
        cancelCount = 0
    }
}
