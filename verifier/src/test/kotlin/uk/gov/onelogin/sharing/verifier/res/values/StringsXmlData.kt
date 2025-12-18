package uk.gov.onelogin.sharing.verifier.res.values

import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.verifier.R

/**
 * Test data used with [StringsXmlTest] for resource IDs that explicitly state a value.
 *
 * Ensures that tests would fail when strings update without updating this test data.
 */
enum class StringsXmlData(val expected: String, @param:StringRes val resourceId: Int) {
    CAMERA_PERMISSION_IS_ENABLED(
        expected = "The camera permission is now enabled.",
        resourceId = R.string.camera_permission_is_enabled
    ),
    CAMERA_PERMISSION_IS_PERMANENTLY_DENIED(
        expected = "The camera permission is permanently denied.",
        resourceId = R.string.camera_permission_is_permanently_denied
    ),
    CANNOT_USE_THE_SCANNED_QR(
        expected = "Cannot use the scanned QR code",
        resourceId = R.string.cannot_use_the_scanned_qr
    ),
    COULD_NOT_DECODE_DEVICE_ENGAGEMENT(
        expected = "Couldn’t decode device engagement!",
        resourceId = R.string.could_not_decode_device_engagement
    ),
    DECODED_DEVICE_ENGAGEMENT_DATA(
        expected = "Decoded device engagement data:",
        resourceId = R.string.decoded_device_engagement_data
    ),
    DID_NOT_SCAN_DIGITAL_CREDENTIAL_QR(
        expected = "The scanned QR code didn’t contain a digital credential. " +
            "Please check the correct QR code to scan, then try again.",
        resourceId = R.string.did_not_scan_digital_credential_qr
    ),
    ENABLE_CAMERA_PERMISSION_TO_CONTINUE(
        expected = "Please enable the camera permission to continue.",
        resourceId = R.string.enable_camera_permission_to_continue
    ),
    OPEN_APP_PERMISSIONS(
        expected = "Open app permissions",
        resourceId = R.string.open_app_permissions
    ),
    SEARCHING_FOR_UUIDS(
        expected = "Searching for UUIDs:",
        resourceId = R.string.searching_for_uuids
    ),
    SUCCESSFULLY_SCANNED_QR_CODE_DATA(
        expected = "Successfully scanned QR code data:",
        resourceId = R.string.successfully_scanned_qr_code_data
    ),
    TRY_AGAIN(
        expected = "Try again",
        resourceId = R.string.try_again
    )
}
