package uk.gov.onelogin.sharing.bluetooth.internal

import android.content.Context
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothCentralComponents
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothCentralFactory
import uk.gov.onelogin.sharing.bluetooth.api.permissions.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.central.AndroidGattClientManager
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBluetoothStateMonitor

class AndroidBluetoothCentralFactory(private val context: Context, private val logger: Logger) :
    BluetoothCentralFactory {
    override fun create(): BluetoothCentralComponents {
        val permissionChecker = BluetoothPermissionChecker(context)

        val gattClientManager = AndroidGattClientManager(
            context = context,
            permissionChecker = permissionChecker,
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
