package uk.gov.onelogin.sharing.orchestration

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.cryptoService.scanner.FakeQrParser
import uk.gov.onelogin.sharing.cryptoService.scanner.QrScanResult
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

class FakeOrchestrator(
    val initialHolderState: MutableStateFlow<HolderSessionState> = MutableStateFlow(
        HolderSessionState.NotStarted
    ),
    val initialVerifierState: MutableStateFlow<VerifierSessionState> = MutableStateFlow(
        VerifierSessionState.NotStarted
    ),
    val parser: FakeQrParser = FakeQrParser(),
    var startCount: Int = 0,
    var cancelCount: Int = 0
) : Orchestrator.Holder,
    Orchestrator.Verifier,
    Resettable {

    override val holderSessionState: StateFlow<HolderSessionState> = initialHolderState
    override val verifierSessionState: StateFlow<VerifierSessionState> = initialVerifierState

    override fun processQrCode(qrCode: String?) {
        when (val result = parser.parse(qrCode)) {
            is QrScanResult.Success -> {
                initialVerifierState.value =
                    VerifierSessionState.Connecting
            }

            is QrScanResult.Invalid -> {
                initialVerifierState.value =
                    VerifierSessionState.Complete.Failed(
                        error = SessionError(
                            message = result.rawValue,
                            exception = IllegalArgumentException("Qr Code is an unsupported format")
                        )
                    )
            }

            QrScanResult.NotFound -> Unit
        }
    }

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
