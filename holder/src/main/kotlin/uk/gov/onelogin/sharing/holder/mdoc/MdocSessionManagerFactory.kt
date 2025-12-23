package uk.gov.onelogin.sharing.holder.mdoc

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import kotlinx.coroutines.CoroutineScope
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothPeripheralFactory

/**
 * A factory for creating a [AndroidMdocSessionManager].
 *
 * Encapsulates the creation and dependency wiring of the components
 * required for the BLE advertiser and GATT server.
 */
@Inject
@ContributesBinding(ViewModelScope::class)
class MdocSessionManagerFactory(
    private val bluetoothPeripheralFactory: BluetoothPeripheralFactory,
    private val logger: Logger
) : SessionManagerFactory {
    override fun create(scope: CoroutineScope): MdocSessionManager {
        val components = bluetoothPeripheralFactory.create()

        return AndroidMdocSessionManager(
            bleAdvertiser = components.advertiser,
            gattServerManager = components.gattServerManager,
            bluetoothStateMonitor = components.bluetoothStateMonitor,
            coroutineScope = scope,
            logger = logger
        )
    }
}
