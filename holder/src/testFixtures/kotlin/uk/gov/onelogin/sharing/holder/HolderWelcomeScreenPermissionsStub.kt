package uk.gov.onelogin.sharing.holder

import android.Manifest
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import uk.gov.onelogin.sharing.bluetooth.BluetoothStateManagerPrompt
import uk.gov.onelogin.sharing.bluetooth.BluetoothStatus
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.core.presentation.permissions.FakePermissionState
import uk.gov.onelogin.sharing.holder.presentation.BluetoothState
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeViewModel

object HolderWelcomeScreenPermissionsStub {

    @OptIn(ExperimentalPermissionsApi::class)
    val fakeGrantedPermissionsState = FakeMultiplePermissionsState(
        permissions = listOf(
            FakePermissionState(
                permission = Manifest.permission.BLUETOOTH_CONNECT,
                status = PermissionStatus.Granted
            ),
            FakePermissionState(
                permission = Manifest.permission.BLUETOOTH_ADVERTISE,
                status = PermissionStatus.Granted
            )
        ),
        onLaunchPermission = { }
    )

    @OptIn(ExperimentalPermissionsApi::class)
    val fakeDeniedPermissionsState = FakeMultiplePermissionsState(
        permissions = listOf(
            FakePermissionState(
                permission = Manifest.permission.BLUETOOTH_CONNECT,
                status = PermissionStatus.Denied(false)
            ),
            FakePermissionState(
                permission = Manifest.permission.BLUETOOTH_ADVERTISE,
                status = PermissionStatus.Denied(false)
            )
        ),
        onLaunchPermission = { }
    )

    @Composable
    fun SetupBluetoothStateManagerPrompt(viewModel: HolderWelcomeViewModel) {
        BluetoothStateManagerPrompt(
            onStateChange = { status ->
                when (status) {
                    BluetoothStatus.BLUETOOTH_OFF -> {
                        viewModel.updateBluetoothState(BluetoothState.Disabled)
                    }

                    BluetoothStatus.BLUETOOTH_ON -> {
                        viewModel.updateBluetoothState(BluetoothState.Enabled)
                    }

                    else -> {
                        viewModel.updateBluetoothState(BluetoothState.Initializing)
                    }
                }
            }
        )
    }
}
