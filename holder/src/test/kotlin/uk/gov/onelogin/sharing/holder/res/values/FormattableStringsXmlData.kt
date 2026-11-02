@file:Suppress("SpreadOperator")

package uk.gov.onelogin.sharing.holder.res.values

import androidx.annotation.Size
import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.res.values.IntRangeHelper.toPlaceholderStrings

enum class FormattableStringsXmlData(
    val translations: Map<String, String>,
    @param:StringRes val resourceId: Int,
    @Size(min = 1) vararg val arguments: Any,
) {
    DVS_IS_CERTIFIED_UNDER_UK_DIGITAL_VERIFICATION_SERVICES_MEANING_THAT(
        translations = mapOf(
            "en" to "<Argument 1> is certified under the " +
                    "UK Digital Verification Services Trust Framework. This means they must:",
            "cy" to "Mae <Argument 1> wedi’i ardystio o dan " +
                    "Fframwaith Ymddiriedaeth Gwasanaethau Gwirio Digidol y DU. " +
                    "Mae hyn yn golygu bod yn rhaid iddynt:",
        ),
        resourceId = R.string.dvs_is_certified_under_uk_digital_verification_services_meaning_that,
        *(1..1).toPlaceholderStrings()
    ),
    DVS_IS_TRUSTED_TO_RECEIVE_AND_STORE_DETAILS(
        translations = mapOf(
            "en" to "<Argument 1> is trusted by the UK government to receive and store your " +
                    "details securely.",
            "cy" to "Mae llywodraeth y DU yn ymddiried yn <Argument 1> i dderbyn a storio eich manylion yn ddiogel."
        ),
        resourceId = R.string.dvs_is_trusted_to_receive_and_store_details,
        *(1..1).toPlaceholderStrings()
    ),
    DVS_S_PRIVACY_NOTICE(
        translations = mapOf(
            "en" to "<Argument 1>’s privacy notice",
            "cy" to "Hysbysiad preifatrwydd <Argument 1>"
        ),
        resourceId = R.string.dvs_s_privacy_notice,
        *(1..1).toPlaceholderStrings()
    ),
    MORE_ABOUT_DVS(
        translations = mapOf(
            "en" to "More about <Argument 1>",
            "cy" to "Mwy am <Argument 1>"
        ),
        resourceId = R.string.more_about_dvs,
        *(1..1).toPlaceholderStrings()
    ),
    READ_DVS_S_PRIVACY_POLICY_TO_FIND_OUT(
        translations = mapOf(
            "en" to "Read <Argument 1>’s privacy notice to find out:",
            "cy" to "Darllenwch hysbysiad preifatrwydd <Argument 1> i ddarganfod:"
        ),
        resourceId = R.string.read_dvs_s_privacy_policy_to_find_out,
        *(1..1).toPlaceholderStrings()
    ),
    THE_PERSON_CHECKING_IS_USING_AN_APP_POWERED_BY_DVS(
        translations = mapOf(
            "en" to "The person doing the check is using an app powered by <Argument 1>.",
            "cy" to "Mae’r person sy’n gwneud y gwiriad yn defnyddio ap sy’n cael ei bweru gan " +
                    "<Argument 1>."
        ),
        resourceId = R.string.the_person_checking_is_using_an_app_powered_by_dvs,
        *(1..1).toPlaceholderStrings()
    )
}
