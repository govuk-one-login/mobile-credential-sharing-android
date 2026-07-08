package uk.gov.onelogin.sharing.orchestration.verifier.session

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothTransport
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.verifier.VerifierCryptoService

/**
 * Manages the ISO 18013-5 session termination protocol.
 *
 * Drives a [TerminationState] state machine through the correct sequence based on
 * whether the holder has already sent a status 20 and whether BLE is still open.
 */
@ContributesBinding(scope = AppScope::class, binding = binding<SessionTerminator>())
@Inject
class SessionTerminatorImpl(
    private val centralBluetoothTransport: CentralBluetoothTransport,
    private val verifierCryptoService: VerifierCryptoService,
    private val logger: Logger
) : SessionTerminator {
    private val _state = MutableStateFlow(TerminationState.IDLE)
    override val state: StateFlow<TerminationState> = _state

    /**
     * Executes the termination protocol.
     *
     * @param serviceUuid The active GATT service UUID for sending messages.
     * @param bleOpen Whether the BLE connection is still active.
     * @param holderRequestedTermination Whether the holder already sent a SessionData with status 20.
     */
    override suspend fun terminate(
        serviceUuid: UUID?,
        bleOpen: Boolean,
        holderRequestedTermination: Boolean
    ) {
        if (!bleOpen) {
            _state.value = TerminationState.TERMINATED
            return
        }

        if (!holderRequestedTermination && serviceUuid != null) {
            _state.value = TerminationState.SENDING_TERMINATION
            logger.debug(logTag, "Sending termination session data")

            val terminationBytes = verifierCryptoService.buildTerminationSessionData()
            val sent = centralBluetoothTransport.sendMessage(
                serviceUuid = serviceUuid,
                data = terminationBytes
            )

            if (sent) {
                _state.value = TerminationState.AWAITING_DELAY
                delay(TERMINATION_DELAY_MS.milliseconds)
            }
        }

        _state.value = TerminationState.SENDING_GATT_END
        logger.debug(logTag, "Sending GATT END command")
        centralBluetoothTransport.sendEnd()

        _state.value = TerminationState.STOPPING
        logger.debug(logTag, "Stopping BLE connection")
        centralBluetoothTransport.stop()

        _state.value = TerminationState.TERMINATED
        logger.debug(logTag, "Session terminated")
    }

    companion object {
        const val TERMINATION_DELAY_MS = 500L
    }
}
