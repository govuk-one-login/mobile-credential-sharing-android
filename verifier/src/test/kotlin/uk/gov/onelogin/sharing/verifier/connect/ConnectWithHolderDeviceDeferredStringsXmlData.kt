package uk.gov.onelogin.sharing.verifier.connect

import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.bluetooth.R as bluetoothR
import uk.gov.onelogin.sharing.verifier.R

/**
 * Test data used with [StringsXmlTest] for resource IDs that defer to another string resource.
 *
 * Ensures that tests would fail when deferred strings update without updating this test data.
 */
enum class ConnectWithHolderDeviceDeferredStringsXmlData(
    @param:StringRes val defersTo: Int,
    @param:StringRes val resourceId: Int
) {
    CONNECT_WITH_HOLDER_BLUETOOTH_STATE(
        defersTo = bluetoothR.string.bluetooth_device_state,
        resourceId = R.string.connect_with_holder_bluetooth_state
    ),
    CONNECT_WITH_HOLDER_PERMISSION_STATE(
        defersTo = bluetoothR.string.bluetooth_permission_state,
        resourceId = R.string.connect_with_holder_permission_state
    )
}
