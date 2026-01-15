package uk.gov.onelogin.sharing.verifier.connect

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import uk.gov.onelogin.sharing.bluetooth.api.scanner.FakeAndroidBluetoothScanner.StubData.dummyByteArray
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsDenied
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsDeniedWithRationale
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsGranted

@OptIn(ExperimentalPermissionsApi::class)
object ConnectWithHolderDeviceEventStubs {
    val permissionUpdateGranted = ConnectWithHolderDeviceEvent.UpdatePermission(
        bluetoothPermissionsGranted
    )
    val permissionUpdateDenied = ConnectWithHolderDeviceEvent.UpdatePermission(
        bluetoothPermissionsDenied
    )
    val permissionUpdateRequiresRationale = ConnectWithHolderDeviceEvent.UpdatePermission(
        bluetoothPermissionsDeniedWithRationale
    )
    val startScanningDummyServiceUuid = ConnectWithHolderDeviceEvent.StartScanning(
        dummyByteArray
    )
}
