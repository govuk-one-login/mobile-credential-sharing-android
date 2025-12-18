package uk.gov.onelogin.sharing.bluetooth.internal

import android.bluetooth.BluetoothManager
import android.content.Context
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothPeripheralComponents
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothPeripheralFactory
import uk.gov.onelogin.sharing.bluetooth.api.permissions.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AndroidBleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AndroidBluetoothAdvertiserProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBleProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.AndroidGattServerManager

/**
 * An Android-specific implementation of the [BluetoothPeripheralFactory] interface.
 *
 * Creates all the necessary components for a BLE server.
 *
 * @param context The Android application context.
 * @param logger An instance of [Logger] for logging events.
 */
@ContributesBinding(ViewModelScope::class)
@Inject
class AndroidBluetoothPeripheralFactory(private val context: Context, private val logger: Logger) :
    BluetoothPeripheralFactory {
    override fun create(): BluetoothPeripheralComponents {
        val adapterProvider = AndroidBluetoothAdapterProvider(context)

        val bleAdvertiser = AndroidBleAdvertiser(
            bleProvider = AndroidBleProvider(
                bluetoothAdapter = adapterProvider,
                bleAdvertiser = AndroidBluetoothAdvertiserProvider(adapterProvider, logger)
            ),
            permissionChecker = BluetoothPermissionChecker(context),
            logger = logger
        )

        val gattServerManager = AndroidGattServerManager(
            context = context,
            bluetoothManager = context.getSystemService(BluetoothManager::class.java),
            permissionsChecker = BluetoothPermissionChecker(context),
            logger = logger
        )

        val bluetoothStateMonitor = AndroidBluetoothStateMonitor(
            appContext = context,
            logger = logger
        )

        return BluetoothPeripheralComponents(
            advertiser = bleAdvertiser,
            gattServerManager = gattServerManager,
            bluetoothStateMonitor = bluetoothStateMonitor
        )
    }
}
