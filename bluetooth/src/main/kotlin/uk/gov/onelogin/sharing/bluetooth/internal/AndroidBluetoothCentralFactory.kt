package uk.gov.onelogin.sharing.bluetooth.internal

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothCentralComponents
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothCentralFactory
import uk.gov.onelogin.sharing.bluetooth.internal.central.AndroidGattClientManager
import uk.gov.onelogin.sharing.bluetooth.internal.central.AndroidGattWriter
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattUuids
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattWriteQueueImpl
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.internal.validator.ServiceValidator
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.core.permission.PermissionChecker

/**
 * An Android-specific implementation of the [BluetoothCentralFactory] interface.
 *
 * Creates all the necessary components for a BLE client.
 *
 * @param context The Android application context.
 * @param logger An instance of [Logger] for logging events.
 */
@ContributesBinding(AppScope::class)
@ContributesBinding(VerifierUiScope::class)
class AndroidBluetoothCentralFactory(
    private val permissionChecker: PermissionChecker,
    private val context: Context,
    private val serviceValidator: ServiceValidator,
    private val logger: Logger
) : BluetoothCentralFactory {
    override fun create(): BluetoothCentralComponents {
        val gattWriter = AndroidGattWriter()

        val gattClientManager = AndroidGattClientManager(
            context = context,
            permissionChecker = permissionChecker,
            serviceValidator = serviceValidator,
            gattWriter = gattWriter,
            logger = logger,
            writeQueue = GattWriteQueueImpl(GattUuids.CLIENT_2_SERVER_UUID)
        )

        val bluetoothStateMonitor = AndroidBluetoothStateMonitor(
            appContext = context,
            logger = logger
        )

        return BluetoothCentralComponents(
            gattClientManager = gattClientManager,
            bluetoothStateMonitor = bluetoothStateMonitor
        )
    }
}
