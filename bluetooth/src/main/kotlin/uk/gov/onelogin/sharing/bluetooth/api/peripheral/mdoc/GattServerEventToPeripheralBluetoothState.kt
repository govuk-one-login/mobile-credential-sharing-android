package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerEvent
import uk.gov.onelogin.sharing.core.Transformer
import uk.gov.onelogin.sharing.core.logger.logTag

class GattServerEventToPeripheralBluetoothState(private val logger: Logger) :
    Transformer<GattServerEvent, PeripheralBluetoothState?> {

    override fun transform(source: GattServerEvent): PeripheralBluetoothState? = when (source) {
        is GattServerEvent.Connected ->
            PeripheralBluetoothState.Connected(source.address)

        is GattServerEvent.Disconnected ->
            PeripheralBluetoothState.Disconnected(source.address, source.isSessionEnd)

        is GattServerEvent.Error ->
            PeripheralBluetoothState.Error(
                PeripheralBluetoothTransportError.fromGattError(source.error)
            )

        is GattServerEvent.ServiceAdded -> {
            logger.debug(logTag, "Service Added: ${source.service.uuid}")
            null
        }

        GattServerEvent.ServiceStopped -> {
            logger.debug(logTag, "GattService Stopped")
            null
        }

        is GattServerEvent.UnsupportedEvent -> {
            logger.error(
                logTag,
                "Unsupported event - status: ${source.status} new state: ${source.newState}"
            )
            null
        }

        GattServerEvent.SessionStarted -> {
            logger.debug(
                logTag,
                "Connection has been setup successfully - session state started"
            )
            null
        }

        is GattServerEvent.SessionEnd -> {
            logger.debug(
                logTag,
                "Session end command was received. Closing connection"
            )
            PeripheralBluetoothState.Ended(source.status)
        }

        is GattServerEvent.MessageReceived ->
            PeripheralBluetoothState.MessageReceived(source.byteArray)
    }
}
