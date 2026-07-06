package uk.gov.onelogin.sharing.orchestration.verifier.session

import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothTransport
import uk.gov.onelogin.sharing.cryptoService.verifier.VerifierCryptoService

class FakeSessionTerminator(
    private val transport: CentralBluetoothTransport? = null,
    private val cryptoService: VerifierCryptoService? = null
) : SessionTerminator {

    private val _state = MutableStateFlow(TerminationState.IDLE)
    override val state: StateFlow<TerminationState> = _state

    var terminateCalls = 0
    var lastServiceUuid: UUID? = null
    var lastBleOpen: Boolean? = null
    var lastHolderRequestedTermination: Boolean? = null

    var buildTerminationSessionDataCalls = 0

    override suspend fun terminate(
        serviceUuid: UUID?,
        bleOpen: Boolean,
        holderRequestedTermination: Boolean
    ) {
        terminateCalls++
        lastServiceUuid = serviceUuid
        lastBleOpen = bleOpen
        lastHolderRequestedTermination = holderRequestedTermination

        if (bleOpen) {
            if (!holderRequestedTermination && serviceUuid != null) {
                buildTerminationSessionDataCalls++
                cryptoService?.buildTerminationSessionData()
            }

            transport?.sendEnd()
        }

        transport?.stop()

        _state.value = TerminationState.TERMINATED
    }
}
