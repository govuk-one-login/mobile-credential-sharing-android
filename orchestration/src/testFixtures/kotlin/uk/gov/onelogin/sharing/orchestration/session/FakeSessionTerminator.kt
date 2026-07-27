package uk.gov.onelogin.sharing.orchestration.session

import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothTransport
import uk.gov.onelogin.sharing.cryptoService.verifier.VerifierCryptoService
import uk.gov.onelogin.sharing.orchestration.verifier.session.SessionTerminator

class FakeSessionTerminator(
    private val transport: CentralBluetoothTransport? = null,
    private val cryptoService: VerifierCryptoService? = null
) : SessionTerminator {

    var terminateCalls = 0
    var lastServiceUuid: UUID? = null
    var lastBleOpen: Boolean? = null
    var lastHolderRequestedTermination: Boolean? = null

    var buildTerminationSessionDataCalls = 0

    override suspend fun terminate(
        serviceUuid: UUID?,
        bleOpen: Boolean,
        holderRequestedTermination: Boolean,
        sendSessionData: Boolean
    ) {
        terminateCalls++
        lastServiceUuid = serviceUuid
        lastBleOpen = bleOpen
        lastHolderRequestedTermination = holderRequestedTermination

        if (bleOpen && !holderRequestedTermination) {
            if (sendSessionData && serviceUuid != null) {
                buildTerminationSessionDataCalls++
                cryptoService?.buildTerminationSessionData()
            }
            transport?.sendEnd()
        }

        transport?.stop()
    }
}
