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
    HOLDER_TURN_ON_BLUETOOTH_TITLE(
        defersTo = R.string.turn_on_bluetooth,
        resourceId = R.string.holder_turn_on_bluetooth_title
    ),
    HOLDER_TURN_ON_BLUETOOTH_BODY_1(
        defersTo = R.string.turn_on_bluetooth_before_sharing,
        resourceId = R.string.holder_turn_on_bluetooth_body_1
    ),
    HOLDER_TURN_ON_BLUETOOTH_BODY_2(
        defersTo = R.string.use_qr_code_to_connect_next,
        resourceId = R.string.holder_turn_on_bluetooth_body_2
    ),
    HOLDER_TURN_ON_BLUETOOTH_BUTTON(
        defersTo = R.string.holder_turn_on_bluetooth_title,
        resourceId = R.string.holder_turn_on_bluetooth_button
    )
}
