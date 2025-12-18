package uk.gov.onelogin.sharing.verifier.session

import java.util.UUID
import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus

/**
 * Represents the verifier session for mDL engagement.
 *
 * Responsible for managing the lifecycle of the verifier's side of the session,
 * including connecting to a holder device and discovering services
 */
interface VerifierSession {
    /**
     * The current state of the verifier session.
     * This can be used to observe the session's progress and errors
     */
    val state: StateFlow<VerifierSessionState>

    /**
     * The current status of the Bluetooth adapter
     */
    val bluetoothStatus: StateFlow<BluetoothStatus>

    /**
     * Starts the verifier session by attempting to connect to the specified service.
     *
     * @param serviceId The [UUID] of the service to connect to.
     */
    fun start(serviceId: UUID)

    /**
     * Stops the verifier session.
     */
    fun stop()
}
