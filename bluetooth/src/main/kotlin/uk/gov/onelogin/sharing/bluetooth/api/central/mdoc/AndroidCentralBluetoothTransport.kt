package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientManager
import uk.gov.onelogin.sharing.bluetooth.api.scanner.BluetoothScanner
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScanEvent
import uk.gov.onelogin.sharing.bluetooth.internal.core.BLE_SEND_NOTIFICATION_DELAY
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates
import uk.gov.onelogin.sharing.core.UUIDExtensions.toUUID
import uk.gov.onelogin.sharing.core.di.ApplicationScope
import uk.gov.onelogin.sharing.core.logger.logTag

@ContributesBinding(scope = AppScope::class)
@SingleIn(AppScope::class)
class AndroidCentralBluetoothTransport(
    private val gattClientManager: GattClientManager,
    private val scanner: BluetoothScanner,
    private val bluetoothStateMonitor: BluetoothStateMonitor,
    @param:ApplicationScope private val coroutineScope: CoroutineScope,
    private val logger: Logger
) : CentralBluetoothTransport {

    private val _state = MutableStateFlow<CentralBluetoothState>(CentralBluetoothState.Idle)
    override val state: StateFlow<CentralBluetoothState> = _state

    private val _bluetoothStatus = MutableStateFlow(BluetoothStatus.UNKNOWN)
    override val bluetoothStatus: StateFlow<BluetoothStatus> = _bluetoothStatus

    private var scanJob: Job? = null

    init {
        coroutineScope.launch {
            gattClientManager.events.collect { handleGattClientEvent(it) }
        }

        coroutineScope.launch {
            bluetoothStateMonitor.states.collect { status ->
                when (status) {
                    BluetoothStatus.OFF,
                    BluetoothStatus.TURNING_OFF -> {
                        _bluetoothStatus.value = BluetoothStatus.OFF
                        scanJob?.cancel()
                        scanJob = null
                        gattClientManager.disconnect()
                        bluetoothStateMonitor.stop()
                    }

                    BluetoothStatus.ON -> _bluetoothStatus.value = BluetoothStatus.ON

                    else -> Unit
                }
            }
        }
    }

    override fun scanAndConnect(serviceUuid: ByteArray) {
        scanJob?.cancel()
        bluetoothStateMonitor.start()
        _state.value = CentralBluetoothState.Scanning

        scanJob = coroutineScope.launch {
            when (val result = scanner.scan(serviceUuid).first()) {
                is ScanEvent.DeviceFound -> {
                    logger.debug(logTag, "Device found: ${result.device.address}")
                    gattClientManager.connect(
                        device = result.device,
                        serviceUuid = serviceUuid.toUUID()
                    )
                }

                is ScanEvent.ScanFailed -> {
                    logger.debug(logTag, "Scan failed: ${result.failure}")
                    _state.value = CentralBluetoothState.Error(
                        CentralBluetoothTransportError.SCAN_FAILED
                    )
                }
            }
        }
    }

    override suspend fun stop() {
        scanJob?.cancel()
        scanJob = null
        notifySessionEnd()
        gattClientManager.disconnect()
        bluetoothStateMonitor.stop()
    }

    private suspend fun notifySessionEnd() {
        val result = gattClientManager.notifySessionEnd()
        if (result == SessionEndStates.SUCCESS) {
            delay(BLE_SEND_NOTIFICATION_DELAY)
        }
    }

    private fun handleGattClientEvent(event: GattClientEvent) {
        when (event) {
            GattClientEvent.Connecting ->
                _state.value = CentralBluetoothState.Connecting

            is GattClientEvent.Connected ->
                _state.value = CentralBluetoothState.Connected(event.deviceAddress)

            is GattClientEvent.Disconnected ->
                _state.value = CentralBluetoothState.Disconnected(
                    event.deviceAddress,
                    event.isSessionEnd
                )

            GattClientEvent.ConnectionStateStarted ->
                _state.value = CentralBluetoothState.ConnectionStateStarted

            is GattClientEvent.Error ->
                _state.value = CentralBluetoothState.Error(
                    CentralBluetoothTransportError.fromClientError(event.error)
                )

            is GattClientEvent.SessionEnd ->
                _state.value = CentralBluetoothState.CentralBluetoothEnded(
                    event.sessionEndStates
                )

            is GattClientEvent.UnsupportedEvent ->
                logger.debug(logTag, "Unhandled event: $event")
        }
    }
}
