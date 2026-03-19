package uk.gov.onelogin.sharing.holder.res.values

import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.holder.R

/**
 * Test data used with [StringsXmlTest] for resource IDs that explicitly state a value.
 *
 * Ensures that tests would fail when strings update without updating this test data.
 */
enum class StringsXmlData(val expected: String, @param:StringRes val resourceId: Int) {
    BLUETOOTH_PERMISSION_PERMANENTLY_DENIED(
        expected = "Bluetooth permissions have been permanently denied",
        resourceId = R.string.bluetooth_permission_permanently_denied
    ),
    BLUETOOTH_TURNED_OFF_HOLDER(
        expected = "Bluetooth was turned off on holder device during session",
        resourceId = R.string.bluetooth_turned_off_holder
    ),
    CHECKING_JOURNEY_REQUIREMENTS(
        expected = "Checking journey requirements...",
        resourceId = R.string.checking_journey_requirements
    ),
    CREATING_QR_CODE(
        expected = "Creating QR code...",
        resourceId = R.string.creating_qr_code
    ),
    ENABLE_BLUETOOTH_PERMISSION(
        expected = "Please enable bluetooth permissions to continue",
        resourceId = R.string.enable_bluetooth_permission
    ),
    GENERATING_QR_CODE_DATA(
        expected = "Generating QR code data...",
        resourceId = R.string.generating_qr_code_data
    ),
    HAVE_NOT_MET_REQUIREMENTS(
        expected = "Haven’t met requirements...",
        resourceId = R.string.have_not_met_requirements
    ),
    OPEN_APP_PERMISSIONS(
        expected = "Open app permissions",
        resourceId = R.string.open_app_permissions
    )
}
