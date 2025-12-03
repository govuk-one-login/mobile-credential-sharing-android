package uk.gov.onelogin.sharing.bluetooth.res.values

import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.bluetooth.R

/**
 * Test data used with [StringsXmlTest] for resource IDs that explicitly state a value.
 *
 * Ensures that tests would fail when strings update without updating this test data.
 */
enum class StringsXmlData(val expected: String, @param:StringRes val resourceId: Int) {
    BLUETOOTH_PERMISSION_STATE(
        expected = "Bluetooth permission state: %1\$s",
        resourceId = R.string.bluetooth_permission_state
    )
}
