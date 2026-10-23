package uk.gov.onelogin.sharing.holder.res.values

import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.holder.R

enum class WelshStringsXmlData(val expected: String, @param:StringRes val resourceId: Int) {
    FIND_OUT_WHICH_DETAILS_TO_SHARE(
        expected = "Darganfyddwch pa fanylion y mae angen i chi eu rhannu",
        resourceId = R.string.find_out_which_details_to_share
    ),
    SHOW_QR_CODE_TO_PERSON_DOING_CHECK(
        expected = "Dangoswch y cod QR i'r person sy'n gwneud y gwiriad.",
        resourceId = R.string.show_qr_code_person_doing_check
    ),
    QR_CODE_LETS_THEM_SEND_LIST_OF_DETAILS(
        expected = "Mae'n caniatáu iddynt anfon rhestr atoch o'r manylion maent angen eu gwirio.",
        resourceId = R.string.qr_code_lets_them_send_list_of_details
    ),
    REVIEW_LIST_BEFORE_AGREEING_TO_SHARE(
        expected = "Byddwch yn adolygu'r rhestr hon cyn cytuno i rannu'ch manylion gyda hwy.",
        resourceId = R.string.review_list_before_agreeing_to_share
    ),
    QR_CODE_TO_GET_LIST_OF_DETAILS(
        expected = "Cod QR i gael y rhestr o fanylion y mae angen i chi eu rhannu",
        resourceId = R.string.qr_code_to_get_list_of_details
    )
}
