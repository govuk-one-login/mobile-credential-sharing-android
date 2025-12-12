package uk.gov.onelogin.sharing.verifier.session

import java.util.UUID
import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus

interface VerifierSession {
    val state: StateFlow<VerifierSessionState>

    val bluetoothStatus: StateFlow<BluetoothStatus>

    fun start(serviceId: UUID)

    fun stop()
}
