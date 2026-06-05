package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.core.MessageSender
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientManager
import uk.gov.onelogin.sharing.bluetooth.api.scanner.BluetoothScanner
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScanEvent
import uk.gov.onelogin.sharing.bluetooth.internal.core.BLE_SEND_NOTIFICATION_DELAY
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates
import uk.gov.onelogin.sharing.core.di.ApplicationScope
import uk.gov.onelogin.sharing.core.logger.logTag

// DCMAW-20404: Expose all bluetooth state events
@ContributesBinding(scope = AppScope::class, binding = binding<CentralBluetoothTransport>())
@SingleIn(AppScope::class)
class AndroidCentralBluetoothTransport(
    private val gattClientManager: GattClientManager,
    private val scanner: BluetoothScanner,
    private val bluetoothStateMonitor: BluetoothStateMonitor,
    @param:ApplicationScope private val coroutineScope: CoroutineScope,
    private val logger: Logger,
    private val ioDispatcher: CoroutineContext = Dispatchers.IO
) : CentralBluetoothTransport,
    MessageSender by gattClientManager {

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
                    BluetoothStatus.TURNING_OFF
                    -> {
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

    override fun scanAndConnect(serviceUuid: UUID) {
        scanJob?.cancel()
        bluetoothStateMonitor.start()
        _state.value = CentralBluetoothState.Scanning

        scanJob = coroutineScope.launch {
            withContext(ioDispatcher) {
                when (val result = scanner.scan(serviceUuid).first()) {
                    is ScanEvent.DeviceFound -> {
                        logger.debug(logTag, "Device found: ${result.device.address}")
                        gattClientManager.connect(
                            device = result.device,
                            serviceUuid = serviceUuid
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
                CentralBluetoothState.Connecting

            is GattClientEvent.Connected ->
                CentralBluetoothState.Connected(event.deviceAddress)

            is GattClientEvent.Disconnected ->
                CentralBluetoothState.Disconnected(
                    event.deviceAddress,
                    event.isSessionEnd
                )

            GattClientEvent.ConnectionStateStarted ->
                CentralBluetoothState.ConnectionStateStarted

            is GattClientEvent.Error ->
                CentralBluetoothState.Error(
                    CentralBluetoothTransportError.fromClientError(event.error)
                )

            is GattClientEvent.SessionEnd ->
                CentralBluetoothState.CentralBluetoothEnded(
                    event.sessionEndStates
                )

            is GattClientEvent.Message -> event.let(CentralBluetoothState::Message)

            is GattClientEvent.UnsupportedEvent -> {
                logger.debug(logTag, "Unhandled event: $event")
                null
            }
        }?.let { bluetoothState ->
            _state.value = bluetoothState
        }.also {
            logger.debug(
                logTag,
                "Completed handling gatt client event: $event"
            )
        }
    }
}
