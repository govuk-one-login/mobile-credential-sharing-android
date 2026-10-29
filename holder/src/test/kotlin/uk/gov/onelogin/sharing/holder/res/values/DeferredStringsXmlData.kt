package uk.gov.onelogin.sharing.holder.res.values

import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.holder.R

/**
 * Test data used with [StringsXmlTest] for resource IDs that defer to another string resource.
 *
 * Ensures that tests would fail when deferred strings update without updating this test data.
 */
enum class DeferredStringsXmlData(
    @param:StringRes val defersTo: Int,
    @param:StringRes val resourceId: Int
) {
    HOLDER_PREREQUISITES_NOT_STARTED(
        defersTo = R.string.checking_journey_requirements,
        resourceId = R.string.holder_prerequisites_not_started
    ),
    HOLDER_PREREQUISITES_PREFLIGHT(
        defersTo = R.string.have_not_met_requirements,
        resourceId = R.string.holder_prerequisites_preflight
    ),
    HOLDER_PREREQUISITES_PRESENTING_ENGAGEMENT(
        defersTo = R.string.creating_qr_code,
        resourceId = R.string.holder_prerequisites_presenting_engagement
    ),
    HOLDER_PREREQUISITES_READY_TO_PRESENT(
        defersTo = R.string.generating_qr_code_data,
        resourceId = R.string.holder_prerequisites_ready_to_present
    ),
    HOLDER_WELCOME_HEADING(
        defersTo = R.string.find_out_which_details_to_share,
        resourceId = R.string.holder_welcome_heading
    ),
    HOLDER_WELCOME_BODY_1(
        defersTo = R.string.show_qr_code_person_doing_check,
        resourceId = R.string.holder_welcome_body_1
    ),
    HOLDER_WELCOME_BODY_2(
        defersTo = R.string.qr_code_lets_them_send_list_of_details,
        resourceId = R.string.holder_welcome_body_2
    ),
    HOLDER_WELCOME_BODY_3(
        defersTo = R.string.review_list_before_agreeing_to_share,
        resourceId = R.string.holder_welcome_body_3
    ),
    HOLDER_WELCOME_QR_CODE_ALT_TEXT(
        defersTo = R.string.qr_code_to_get_list_of_details,
        resourceId = R.string.holder_welcome_qr_code_alt_text
    )
}
