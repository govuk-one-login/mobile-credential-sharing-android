package uk.gov.onelogin.sharing.holder.mdoc

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import kotlinx.coroutines.CoroutineScope
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothServerFactory

/**
 * A factory for creating a [uk.gov.onelogin.sharing.holder.mdoc.AndroidMdocSessionManager].
 *
 * Encapsulates the creation and dependency wiring of the components
 * required for the BLE advertiser and GATT server.
 */
@ContributesBinding(ViewModelScope::class)
@Inject
class MdocSessionManagerFactory(
    private val bluetoothFactory: BluetoothServerFactory,
    private val logger: Logger
) : SessionManagerFactory {
    override fun create(scope: CoroutineScope): MdocSessionManager {
        val components = bluetoothFactory.createServer(scope)

        return AndroidMdocSessionManager(
            bleAdvertiser = components.advertiser,
            gattServerManager = components.gattServer,
            bluetoothStateMonitor = components.bluetoothStateMonitor,
            coroutineScope = scope,
            logger = logger
        )
    }
}
