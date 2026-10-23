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
    ),
    FIND_OUT_WHICH_DETAILS_TO_SHARE(
        expected = "Find out which details you need to share",
        resourceId = R.string.find_out_which_details_to_share
    ),
    SHOW_QR_CODE_TO_PERSON_DOING_CHECK(
        expected = "Show the QR code to the person doing the check.",
        resourceId = R.string.show_qr_code_person_doing_check
    ),
    QR_CODE_LETS_THEM_SEND_LIST_OF_DETAILS(
        expected = "It lets them send you a list of the details they need to check.",
        resourceId = R.string.qr_code_lets_them_send_list_of_details
    ),
    REVIEW_LIST_BEFORE_AGREEING_TO_SHARE(
        expected = "You’ll review this list before agreeing to share your details with them.",
        resourceId = R.string.review_list_before_agreeing_to_share
    ),
    QR_CODE_TO_GET_LIST_OF_DETAILS(
        expected = "QR code to get the list of details you need to share",
        resourceId = R.string.qr_code_to_get_list_of_details
    )
}
