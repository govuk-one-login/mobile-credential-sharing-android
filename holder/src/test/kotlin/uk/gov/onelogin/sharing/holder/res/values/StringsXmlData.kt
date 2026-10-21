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
    HOLDER_CONSENT_CARD_HEADING(
        expected = "You\u2019ll share %1\$d details from your %2\$s",
        resourceId = R.string.holder_consent_card_heading
    ),
    HOLDER_CONSENT_CARD_WILL_KEEP(
        expected = "%1\$s will keep these details as a record of this check:",
        resourceId = R.string.holder_consent_card_will_keep
    ),
    HOLDER_CONSENT_CARD_WILL_NOT_KEEP(
        expected = "%1\$s will not keep:",
        resourceId = R.string.holder_consent_card_will_not_keep
    ),
    HOLDER_CONSENT_CARD_ALL_KEPT(
        expected = "%1\$s will keep these details as a record of this check.",
        resourceId = R.string.holder_consent_card_all_kept
    ),
    HOLDER_CONSENT_CARD_NONE_KEPT(
        expected = "These details will only be used for this check. They will not be saved.",
        resourceId = R.string.holder_consent_card_none_kept
    ),
    HOLDER_CONSENT_DOC_MDL(
        expected = "UK driving licence",
        resourceId = R.string.holder_consent_doc_mdl
    ),
    HOLDER_CONSENT_ATTR_DOCUMENT_NUMBER(
        expected = "Licence number",
        resourceId = R.string.holder_consent_attr_document_number
    ),
    HOLDER_CONSENT_ATTR_PORTRAIT(
        expected = "Photo",
        resourceId = R.string.holder_consent_attr_portrait
    ),
    HOLDER_CONSENT_ATTR_TITLE(
        expected = "Title",
        resourceId = R.string.holder_consent_attr_title
    ),
    HOLDER_CONSENT_ATTR_FAMILY_NAME(
        expected = "Last name",
        resourceId = R.string.holder_consent_attr_family_name
    ),
    HOLDER_CONSENT_ATTR_GIVEN_NAME(
        expected = "First name(s)",
        resourceId = R.string.holder_consent_attr_given_name
    ),
    HOLDER_CONSENT_ATTR_EXPIRY_DATE(
        expected = "Date of expiry",
        resourceId = R.string.holder_consent_attr_expiry_date
    ),
    HOLDER_CONSENT_ATTR_BIRTH_DATE(
        expected = "Date of birth",
        resourceId = R.string.holder_consent_attr_birth_date
    ),
    HOLDER_CONSENT_ATTR_AGE_OVER(
        expected = "If you\u2019re over %1\$d",
        resourceId = R.string.holder_consent_attr_age_over
    ),
    HOLDER_CONSENT_ATTR_BIRTH_PLACE(
        expected = "Place of birth",
        resourceId = R.string.holder_consent_attr_birth_place
    ),
    HOLDER_CONSENT_ATTR_RESIDENT_ADDRESS(
        expected = "Street address",
        resourceId = R.string.holder_consent_attr_resident_address
    ),
    HOLDER_CONSENT_ATTR_RESIDENT_CITY(
        expected = "Town or city (address)",
        resourceId = R.string.holder_consent_attr_resident_city
    ),
    HOLDER_CONSENT_ATTR_RESIDENT_POSTAL_CODE(
        expected = "Postcode",
        resourceId = R.string.holder_consent_attr_resident_postal_code
    ),
    HOLDER_CONSENT_ATTR_ISSUE_DATE(
        expected = "Date of issue",
        resourceId = R.string.holder_consent_attr_issue_date
    ),
    HOLDER_CONSENT_ATTR_ISSUING_AUTHORITY(
        expected = "Issuing authority",
        resourceId = R.string.holder_consent_attr_issuing_authority
    ),
    HOLDER_CONSENT_ATTR_ISSUING_COUNTRY(
        expected = "Issuing country",
        resourceId = R.string.holder_consent_attr_issuing_country
    ),
    HOLDER_CONSENT_ATTR_UN_DISTINGUISHING_SIGN(
        expected = "Issuing country (UN distinguishing sign)",
        resourceId = R.string.holder_consent_attr_un_distinguishing_sign
    ),
    HOLDER_CONSENT_ATTR_DRIVING_PRIVILEGES(
        expected = "Vehicles you can drive",
        resourceId = R.string.holder_consent_attr_driving_privileges
    ),
    HOLDER_CONSENT_ATTR_PROVISIONAL_DRIVING_PRIVILEGES(
        expected = "Vehicles you can drive with your provisional licence",
        resourceId = R.string.holder_consent_attr_provisional_driving_privileges
    ),
    HOLDER_CONSENT_ATTR_WELSH_LICENCE(
        expected = "If your licence is bilingual (English and Welsh)",
        resourceId = R.string.holder_consent_attr_welsh_licence
    )
}
