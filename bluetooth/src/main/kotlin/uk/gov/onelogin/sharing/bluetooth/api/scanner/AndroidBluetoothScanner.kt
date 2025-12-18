package uk.gov.onelogin.sharing.bluetooth.api.scanner

import android.Manifest
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import androidx.annotation.RequiresPermission
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import java.util.concurrent.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.adapter.BluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScannerCallback.Companion.toLeScanCallback
import uk.gov.onelogin.sharing.core.UUIDExtensions.toUUID
import uk.gov.onelogin.sharing.core.logger.logTag

@ContributesBinding(ViewModelScope::class)
@Inject
class AndroidBluetoothScanner(
    bluetoothAdapterProvider: BluetoothAdapterProvider,
    val logger: Logger
) : BluetoothScanner {

    private val scanner = bluetoothAdapterProvider.getLeScanner()
    private var isScanning = false

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Suppress("LongMethod")
    override fun scan(serviceUuid: ByteArray): Flow<ScanEvent> = callbackFlow {
        lateinit var leScanCallback: ScanCallback
        when {
            isScanning -> {
                logger.error(logTag, "Scan failed: A scan is already in progress")
                trySend(ScanEvent.ScanFailed(ScannerFailure.ALREADY_STARTED_SCANNING))
                awaitClose {
                    cancel(CancellationException("Scan failed: A scan is already in progress"))
                }
                return@callbackFlow
            }

            scanner == null -> {
                logger.error(logTag, "Scan failed: Scanner is null")
                trySend(ScanEvent.ScanFailed(ScannerFailure.INTERNAL_ERROR))
                awaitClose { cancel(CancellationException("Scan failed: Scanner is null")) }
                return@callbackFlow
            }

            else -> {
                val callbackLogic = ScannerCallback.of(
                    onResult = { _, result ->
                        logger.debug(
                            logTag,
                            " Found device: ${result.device.address}"
                        )
                        trySend(ScanEvent.DeviceFound(result.device.address))
                    },
                    onFailure = { failure ->
                        isScanning = false
                        trySend(ScanEvent.ScanFailed(failure))
                        cancel(CancellationException("Scan failed: $failure"))
                    }
                )

                leScanCallback = callbackLogic.toLeScanCallback()

                logger.debug(
                    logTag,
                    "Creating ScanFilter for UUID: ${serviceUuid.toUUID()}"
                )

                val filters: List<ScanFilter> = listOf(
                    ScanFilter.Builder()
                        .setServiceUuid(ParcelUuid(serviceUuid.toUUID()))
                        .build()
                )

                isScanning = true

                scanner.startScan(
                    filters,
                    ScanSettings.Builder()
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setLegacy(false)
                        .build(),
                    leScanCallback
                )
            }
        }

        awaitClose {
            logger.debug(logTag, "Stopping scanner")
            isScanning = false
            scanner.stopScan(leScanCallback)
        }
    }
}
