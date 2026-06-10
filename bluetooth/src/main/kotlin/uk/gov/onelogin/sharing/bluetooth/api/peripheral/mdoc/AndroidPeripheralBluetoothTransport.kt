package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.advertising.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.api.advertising.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.api.advertising.BleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.api.advertising.StartAdvertisingException
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.core.MessageSender
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerManager
import uk.gov.onelogin.sharing.bluetooth.internal.core.BLE_SEND_NOTIFICATION_DELAY
import uk.gov.onelogin.sharing.core.coroutines.CoroutineNameExt.asCoroutineName
import uk.gov.onelogin.sharing.core.di.ApplicationScope
import uk.gov.onelogin.sharing.core.logger.logTag

// DCMAW-20404: Expose all bluetooth state events
@ContributesBinding(scope = AppScope::class, binding = binding<PeripheralBluetoothTransport>())
class AndroidPeripheralBluetoothTransport(
    private val bleAdvertiser: BleAdvertiser,
    private val gattServerManager: GattServerManager,
    private val bluetoothStateMonitor: BluetoothStateMonitor,
    @param:ApplicationScope private val coroutineScope: CoroutineScope,
    private val logger: Logger,
    private val ioDispatcher: CoroutineContext = Dispatchers.IO,
) : PeripheralBluetoothTransport,
    MessageSender by gattServerManager {

    private val serverEventTransformer = GattServerEventToPeripheralBluetoothState(logger)

    private val _state = MutableStateFlow<PeripheralBluetoothState>(PeripheralBluetoothState.Idle)
    override val state: StateFlow<PeripheralBluetoothState> = _state

    internal var monitoringJob: Job = monitorServerEvents()

    private fun cancelCurrentJobs() {
        monitoringJob.cancel()
        monitoringJob = monitorServerEvents()
    }

    private fun monitorServerEvents(): Job = coroutineScope.launch(
        ioDispatcher + "$logTag.BluetoothMonitoring".asCoroutineName(),
        start = CoroutineStart.LAZY
    ) {
        launch("$logTag.MonitorBluetoothState".asCoroutineName()) {
            bluetoothStateMonitor.states.filter(BluetoothStatus::isOff).map {
                PeripheralBluetoothState.Error(
                    PeripheralBluetoothTransportError.BLUETOOTH_TURNED_OFF
                )
            }.collect { error ->
                _state.value = error
            }
        }
        launch("$logTag.HandleAdvertiserState".asCoroutineName()) {
            bleAdvertiser.state.collect {
                handleAdvertiserState(it)
            }
        }
        launch("$logTag.HandleGattServerEvent".asCoroutineName()) {
            gattServerManager.events.collect {
                handleGattEvent(it)
            }
        }
    }

    override suspend fun start(serviceUuid: UUID): Unit = withContext(
        ioDispatcher + "$logTag.Start".asCoroutineName()
    ) {
        cancelCurrentJobs()

        monitoringJob.start()
        bluetoothStateMonitor.start()
        try {
            bleAdvertiser.startAdvertise(BleAdvertiseData(serviceUuid))
        } catch (e: StartAdvertisingException) {
            logger.error(logTag, "Error starting advertising: ${e.error}", e)
            _state.value =
                PeripheralBluetoothState.Error(PeripheralBluetoothTransportError.ADVERTISING_FAILED)
        }

        gattServerManager.open(serviceUuid)
    }

    override suspend fun stop(
        serviceUuid: UUID,
        sendEndCommand: Boolean,
    ): Unit = withContext(ioDispatcher + "$logTag.Stop".asCoroutineName()) {
        cancelCurrentJobs()
        if (sendEndCommand) {
            notifySessionEnd(serviceUuid)
        }
        bleAdvertiser.stopAdvertise()
        gattServerManager.close()
        bluetoothStateMonitor.stop()
    }

    override suspend fun notifySessionEnd(serviceUuid: UUID): Unit = withContext(
        ioDispatcher + "$logTag.NotifySessionEnd".asCoroutineName()
    ) {
        val result = gattServerManager.notifySessionEnd(serviceUuid)
        if (result == SessionEndStateQueued.Success) {
            // allow time for the END notification to be sent before closing the GATT server
            delay(BLE_SEND_NOTIFICATION_DELAY.milliseconds)
        }
    }

    private fun handleAdvertiserState(state: AdvertiserState) {
        if (state is AdvertiserState.Failed) {
            _state.value =
                PeripheralBluetoothState.Error(
                    PeripheralBluetoothTransportError.ADVERTISING_FAILED
                )
        }
        logger.debug(logTag, "Advertising ${state::class.java.simpleName}")
    }

    private fun handleGattEvent(event: GattServerEvent) {
        event.let(serverEventTransformer::transform)?.let { bluetoothState ->
            _state.value = bluetoothState
        }.also {
            logger.debug(
                logTag,
                "Completed handling gatt server event: $event"
            )
        }
    }
}

