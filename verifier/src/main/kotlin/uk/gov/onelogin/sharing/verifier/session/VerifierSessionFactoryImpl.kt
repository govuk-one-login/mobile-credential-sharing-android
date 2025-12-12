package uk.gov.onelogin.sharing.verifier.session

import kotlinx.coroutines.CoroutineScope
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothCentralFactory

class VerifierSessionFactoryImpl(
    private val bluetoothCentralFactory: BluetoothCentralFactory,
    private val serviceValidator: ServiceValidator,
    private val logger: Logger
) : VerifierSessionFactory {
    override fun create(scope: CoroutineScope): VerifierSession {
        val components = bluetoothCentralFactory.create()

        return MdocVerifierSession(
            gattClientManager = components.gattClientManager,
            bluetoothStateMonitor = components.bluetoothStateMonitor,
            serviceValidator = serviceValidator,
            logger = logger,
            scope = scope
        )
    }
}
