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
    GET_READY_TO_SHARE_ALLOW_BLUETOOTH(
        defersTo = R.string.allow_bluetooth,
        resourceId = R.string.get_ready_to_share_allow_bluetooth
    ),
    GET_READY_TO_SHARE_HEADER(
        defersTo = R.string.get_ready_to_share,
        resourceId = R.string.get_ready_to_share_header
    ),
    GET_READY_TO_SHARE_FIND_OUT_MORE(
        defersTo = R.string.find_out_how_sharing_works,
        resourceId = R.string.get_ready_to_share_find_out_more
    ),
    GET_READY_TO_SHARE_BEFORE_SHARING(
        resourceId = R.string.get_ready_to_share_before_sharing,
        defersTo = R.string.allow_bluetooth_before_sharing_details
    ),
    GET_READY_TO_SHARE_IF_BLUETOOTH_IS_OFF(
        defersTo = R.string.you_will_need_to_turn_on_bluetooth,
        resourceId = R.string.get_ready_to_share_if_bluetooth_is_off,
    ),
    GET_READY_TO_SHARE_USE_QR_TO_CONNECT(
        defersTo = R.string.you_will_connect_via_qr_later,
        resourceId = R.string.get_ready_to_share_use_qr_to_connect,
    ),
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

}
