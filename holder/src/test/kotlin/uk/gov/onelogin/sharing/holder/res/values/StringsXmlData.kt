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
    ),
    BLUETOOTH_PERMISSION_DENIED(
        expected = "Bluetooth permissions were denied",
        resourceId = R.string.bluetooth_permission_denied
    ),
    HOLDER_AWAITING_RESOLUTION_TITLE(
        expected = "Details shared",
        resourceId = R.string.holder_awaiting_resolution_title
    ),
    HOLDER_CONSENT_DENY_DIALOG_TITLE(
        expected = "Are you sure you want to deny this request?",
        resourceId = R.string.holder_consent_deny_dialog_title
    ),
    HOLDER_CONSENT_DENY_DIALOG_DISMISS(
        expected = "Cancel",
        resourceId = R.string.holder_consent_deny_dialog_dismiss
    ),
    HOLDER_SUCCESS_UNFULFILLABLE_REQUEST_TITLE(
        expected = "Unfulfillable request",
        resourceId = R.string.holder_success_unfulfillable_request_title
    )
}
