package uk.gov.onelogin.sharing.bluetooth.internal

import android.content.Context
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothCentralComponents
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothCentralFactory
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.central.AndroidGattClientManager
import uk.gov.onelogin.sharing.bluetooth.internal.central.AndroidGattWriter
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.internal.validator.ServiceValidator

/**
 * An Android-specific implementation of the [BluetoothCentralFactory] interface.
 *
 * Creates all the necessary components for a BLE client.
 *
 * @param context The Android application context.
 * @param logger An instance of [Logger] for logging events.
 */
@ContributesBinding(ViewModelScope::class)
class AndroidBluetoothCentralFactory(
    private val bluetoothPermissionChecker: BluetoothPermissionChecker,
    private val context: Context,
    private val serviceValidator: ServiceValidator,
    private val logger: Logger
) : BluetoothCentralFactory {
    override fun create(): BluetoothCentralComponents {
        val gattWriter = AndroidGattWriter()

        val gattClientManager = AndroidGattClientManager(
            context = context,
            permissionChecker = bluetoothPermissionChecker,
            serviceValidator = serviceValidator,
            gattWriter = gattWriter,
            logger = logger
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
