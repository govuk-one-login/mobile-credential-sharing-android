package uk.gov.onelogin.sharing.bluetooth.api

import android.bluetooth.BluetoothManager
import android.content.Context
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import kotlinx.coroutines.CoroutineScope
import uk.gov.onelogin.sharing.bluetooth.api.permissions.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.AndroidMdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AndroidBleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AndroidBluetoothAdvertiserProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBleProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.AndroidGattServerManager
import uk.gov.onelogin.sharing.core.logger.StandardLoggerFactory

/**
 * A factory for creating a [AndroidMdocSessionManager].
 *
 * Encapsulates the creation and dependency wiring of the components
 * required for the BLE advertiser and GATT server.
 */
@ContributesBinding(ViewModelScope::class)
@Inject
class MdocSessionManagerFactory(private val context: Context) : SessionManagerFactory {
    /**
     * Constructs and configures all the necessary dependencies for a
     * [AndroidMdocSessionManager].
     *
     * @param scope The [CoroutineScope] in which the session manager will be launched in.
     * @return A fully configured [MdocSessionManager] instance.
     */
    override fun create(scope: CoroutineScope): MdocSessionManager {
        val logger = StandardLoggerFactory.create()
        val adapterProvider = AndroidBluetoothAdapterProvider(context)
        val bleAdvertiser = AndroidBleAdvertiser(
            bleProvider = AndroidBleProvider(
                bluetoothAdapter = adapterProvider,
                bleAdvertiser = AndroidBluetoothAdvertiserProvider(
                    bluetoothAdapter = adapterProvider,
                    logger = logger
                )
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

        return AndroidMdocSessionManager(
            bleAdvertiser = bleAdvertiser,
            gattServerManager = gattServerManager,
            bluetoothStateMonitor = bluetoothStateMonitor,
            coroutineScope = scope,
            logger = logger
        )
    }
}
