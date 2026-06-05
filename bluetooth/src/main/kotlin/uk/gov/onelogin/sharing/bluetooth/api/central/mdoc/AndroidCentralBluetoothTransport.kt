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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
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
import uk.gov.onelogin.sharing.core.coroutines.CoroutineNameExt.asCoroutineName
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
    private val ioDispatcher: CoroutineContext = Dispatchers.IO,
) : CentralBluetoothTransport,
    MessageSender by gattClientManager {

    private val clientEventTransformer = GattClientEventToCentralBluetoothState(logger)
    private val _state = MutableStateFlow<CentralBluetoothState>(CentralBluetoothState.Idle)
    override val state: StateFlow<CentralBluetoothState> = _state

    internal val bluetoothStatus: StateFlow<BluetoothStatus> =
        bluetoothStateMonitor.states.map { status ->
            if (status.isOff()) {
                _state.value = CentralBluetoothState.Error(
                    CentralBluetoothTransportError.BLUETOOTH_TURNED_OFF
                )
            }

            status
        }.stateIn(
            coroutineScope.plus(ioDispatcher),
            SharingStarted.Eagerly,
            BluetoothStatus.UNKNOWN
        )

    internal var scanJob: Job? = null
    internal var monitoringJob: Job? = null

    private fun cancelCurrentJobs() {
        scanJob?.cancel()
        monitoringJob?.cancel()

        scanJob = null
        monitoringJob = null
    }

    override suspend fun scanAndConnect(serviceUuid: UUID) {
        cancelCurrentJobs()

        monitoringJob = monitorClientEvents()
        bluetoothStateMonitor.start()
        _state.value = CentralBluetoothState.Scanning

        scanJob = coroutineScope.launch(
            ioDispatcher + "$logTag.ScanAndConnect".asCoroutineName()
        ) {
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

    override suspend fun stop() {
        withContext(ioDispatcher + "$logTag.Stop".asCoroutineName()) {
            cancelCurrentJobs()
            notifySessionEnd()
            gattClientManager.disconnect()
            bluetoothStateMonitor.stop()
        }
    }

    private suspend fun notifySessionEnd() {
        val result = gattClientManager.notifySessionEnd()
        if (result == SessionEndStates.SUCCESS) {
            delay(BLE_SEND_NOTIFICATION_DELAY)
        }
    }

    internal fun monitorClientEvents(): Job = coroutineScope.launch(
        ioDispatcher + "$logTag.BluetoothMonitoring".asCoroutineName()
    ) {
        launch("$logTag.HandleGattClientEvent".asCoroutineName()) {
            gattClientManager.events.collect(::handleGattClientEvent)
        }
    }

    private fun handleGattClientEvent(event: GattClientEvent) {
        event.let(clientEventTransformer::transform)?.let { bluetoothState ->
            _state.value = bluetoothState
        }.also {
            logger.debug(
                logTag,
                "Completed handling gatt client event: $event"
            )
        }
    }
}
