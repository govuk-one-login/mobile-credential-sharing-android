package uk.gov.onelogin.sharing.verifier.session

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothCentralFactory
import uk.gov.onelogin.sharing.core.VerifierUiScope

@ContributesBinding(AppScope::class)
@ContributesBinding(VerifierUiScope::class)
class VerifierSessionFactoryImpl(
    private val bluetoothCentralFactory: BluetoothCentralFactory,
    private val logger: Logger
) : VerifierSessionFactory {
    override fun create(scope: CoroutineScope): VerifierSession {
        val components = bluetoothCentralFactory.create()

        return MdocVerifierSession(
            gattClientManager = components.gattClientManager,
            bluetoothStateMonitor = components.bluetoothStateMonitor,
            logger = logger,
            scope = scope
        )
    }
}
