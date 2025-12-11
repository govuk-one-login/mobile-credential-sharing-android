package uk.gov.onelogin.sharing.bluetooth.internal

import android.bluetooth.BluetoothManager
import android.content.Context
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import kotlinx.coroutines.CoroutineScope
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothServerComponents
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothServerFactory
import uk.gov.onelogin.sharing.bluetooth.api.permissions.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AndroidBleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AndroidBluetoothAdvertiserProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBleProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.AndroidGattServerManager

@ContributesBinding(ViewModelScope::class)
@Inject
class AndroidBluetoothServerFactory(private val context: Context, private val logger: Logger) :
    BluetoothServerFactory {

    override fun createServer(scope: CoroutineScope): BluetoothServerComponents {
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

        return BluetoothServerComponents(
            advertiser = bleAdvertiser,
            gattServer = gattServerManager,
            bluetoothStateMonitor = bluetoothStateMonitor
        )
    }
}
