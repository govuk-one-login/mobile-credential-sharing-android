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
        resourceId = R.string.get_ready_to_share_if_bluetooth_is_off
    ),
    GET_READY_TO_SHARE_USE_QR_TO_CONNECT(
        defersTo = R.string.you_will_connect_via_qr_later,
        resourceId = R.string.get_ready_to_share_use_qr_to_connect
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
    MORE_ABOUT_DVS_TITLE(
        defersTo = R.string.more_about_dvs,
        resourceId = R.string.more_about_dvs_title
    ),
    MORE_ABOUT_DVS_BODY_1(
        defersTo = R.string.the_person_checking_is_using_an_app_powered_by_dvs,
        resourceId = R.string.more_about_dvs_body_1
    ),
    MORE_ABOUT_DVS_BODY_2(
        defersTo = R.string.dvs_is_trusted_to_receive_and_store_details,
        resourceId = R.string.more_about_dvs_body_2
    ),
    MORE_ABOUT_DVS_BODY_3(
        defersTo = R.string.dvs_is_certified_under_uk_digital_verification_services_meaning_that,
        resourceId = R.string.more_about_dvs_body_3
    ),
    MORE_ABOUT_DVS_BULLET_1(
        defersTo = R.string.use_encryption_when_storing_your_details,
        resourceId = R.string.more_about_dvs_bullet_1
    ),
    MORE_ABOUT_DVS_BULLET_2(
        defersTo = R.string.keep_your_details_only_as_long_as_needed,
        resourceId = R.string.more_about_dvs_bullet_2
    ),
    MORE_ABOUT_DVS_BULLET_3(
        defersTo = R.string.never_use_your_details_for_marketing_purposes,
        resourceId = R.string.more_about_dvs_bullet_3
    ),
    MORE_ABOUT_DVS_PRIVACY_SUBHEADING(
        defersTo = R.string.dvs_s_privacy_notice,
        resourceId = R.string.more_about_dvs_privacy_subheading
    ),
    MORE_ABOUT_DVS_PRIVACY_BULLET_1(
        defersTo = R.string.how_long_they_will_keep_your_details,
        resourceId = R.string.more_about_dvs_privacy_bullet_1,
    ),
    MORE_ABOUT_DVS_PRIVACY_BULLET_2(
        defersTo = R.string.how_your_details_can_be_used,
        resourceId = R.string.more_about_dvs_privacy_bullet_2,
    ),
    MORE_ABOUT_DVS_PRIVACY_NOTICE_LINK(
        defersTo = R.string.dvs_s_privacy_notice,
        resourceId = R.string.more_about_dvs_privacy_policy_link
    ),
    MORE_ABOUT_DVS_PRIVACY_BODY(
        defersTo = R.string.read_dvs_s_privacy_policy_to_find_out,
        resourceId = R.string.more_about_dvs_privacy_body
    )
}
