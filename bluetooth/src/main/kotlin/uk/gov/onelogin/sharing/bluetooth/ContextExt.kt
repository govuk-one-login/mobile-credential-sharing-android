package uk.gov.onelogin.sharing.bluetooth

import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import android.os.UserManager

object ContextExt {
    /**
     * [Context] extension function that requests the [Context.BLUETOOTH_SERVICE] as an instance of
     * [BluetoothManager].
     *
     * @return A [BluetoothManager] instance if the [Context] has one. Otherwise, `null`.
     */
    val Context.bluetoothManager: BluetoothManager? get() = getSystemService(
        Context.BLUETOOTH_SERVICE
    ) as? BluetoothManager

    val Context.locationManager: LocationManager? get() = getSystemService(
        Context.LOCATION_SERVICE
    ) as? LocationManager

    val Context.userManager: UserManager? get() = getSystemService(
        Context.USER_SERVICE
    ) as? UserManager

    val Context.devicePolicyManager: DevicePolicyManager? get() = getSystemService(
        Context.DEVICE_POLICY_SERVICE
    ) as? DevicePolicyManager
}
