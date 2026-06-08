package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.core.Transformer
import uk.gov.onelogin.sharing.core.logger.logTag

class GattClientEventToCentralBluetoothState(private val logger: Logger) :
    Transformer<GattClientEvent, CentralBluetoothState?> {
    override fun transform(source: GattClientEvent): CentralBluetoothState? = when (source) {
        GattClientEvent.Connecting -> CentralBluetoothState.Connecting

        is GattClientEvent.Connected -> CentralBluetoothState.Connected(source.deviceAddress)

        is GattClientEvent.Disconnected -> CentralBluetoothState.Disconnected(
            source.deviceAddress,
            source.isSessionEnd
        )

        GattClientEvent.ConnectionStateStarted -> CentralBluetoothState.ConnectionStateStarted

        is GattClientEvent.Error -> CentralBluetoothState.Error(
            CentralBluetoothTransportError.fromClientError(source.error)
        )

        is GattClientEvent.SessionEnd -> CentralBluetoothState.CentralBluetoothEnded(
            source.sessionEndStates
        )

        is GattClientEvent.Message -> source.let(CentralBluetoothState::Message)

        is GattClientEvent.UnsupportedEvent -> {
            logger.debug(logTag, "Unhandled event: $source")
            null
        }
    }
}
