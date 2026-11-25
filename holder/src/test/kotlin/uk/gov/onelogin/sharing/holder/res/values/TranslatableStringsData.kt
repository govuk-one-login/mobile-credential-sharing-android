package uk.gov.onelogin.sharing.holder.res.values

import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.holder.R

enum class TranslatableStringsData(
    private val translations: Map<String, String>,
    @param:StringRes val resourceId: Int
) {
    ALLOW_BLUETOOTH(
        resourceId = R.string.allow_bluetooth,
        translations = mapOf(
            "en" to "Allow Bluetooth",
            "cy" to "Caniatáu Bluetooth"
        )
    ),
    ALLOW_BLUETOOTH_BEFORE_SHARING_DETAILS(
        resourceId = R.string.allow_bluetooth_before_sharing_details,
        translations = mapOf(
            "en" to "Before you can share details from your UK driving licence, " +
                "you need to allow this app to use Bluetooth.",
            "cy" to "Cyn y gallwch rannu manylion o’ch trwydded yrru’r DU, " +
                "mae angen i chi ganiatáu i’r ap hwn ddefnyddio Bluetooth."
        )
    ),
    FIND_OUT_HOW_SHARING_WORKS(
        resourceId = R.string.find_out_how_sharing_works,
        translations = mapOf(
            "en" to "Find out how sharing works",
            "cy" to "Darganfyddwch sut mae rhannu yn gweithio"
        )
    ),
    BLUETOOTH_IS_RESTRICTED(
        resourceId = R.string.bluetooth_is_restricted,
        translations = mapOf(
            "en" to "Bluetooth is restricted",
            "cy" to "Mae Bluetooth wedi’i gyfyngu"
        )
    ),
    YOU_CANNOT_SHARE_YOUR_DETAILS(
        resourceId = R.string.you_cannot_share_your_details,
        translations = mapOf(
            "en" to "This means you cannot share your details.",
            "cy" to "Mae hyn yn golygu na allwch rannu eich manylion."
        )
    ),
    TURN_OFF_PARENTAL_CONTROLS_RESTRICTING_BLUETOOTH(
        resourceId = R.string.turn_off_parental_controls_restricting_bluetooth,
        translations = mapOf(
            "en" to "If this phone has parental controls restricting Bluetooth, " +
                "turn them off in your phone settings.",
            "cy" to "Os oes gan y ffôn hwn reolaethau rhieni sy’n cyfyngu ar Bluetooth, " +
                "diffoddwch nhw yng ngosodiadau eich ffôn."
        )
    ),
    ASK_ADMINISTRATOR_TO_ALLOW_BLUETOOTH(
        resourceId = R.string.ask_administrator_to_allow_bluetooth,
        translations = mapOf(
            "en" to "If an organisation manages this phone, " +
                "ask an administrator to allow Bluetooth.",
            "cy" to "Os yw sefydliad yn rheoli’r ffôn hwn, " +
                "gofynnwch i weinyddwr ganiatáu Bluetooth."
        )
    ),
    GET_READY_TO_SHARE(
        resourceId = R.string.get_ready_to_share,
        translations = mapOf(
            "en" to "Get ready to share",
            "cy" to "Paratoi i rannu"
        )
    ),
    GO_BACK_TO_DOCUMENTS(
        resourceId = R.string.go_back_to_documents,
        translations = mapOf(
            "en" to "Go back to documents",
            "cy" to "Ewch yn ôl i dogfennau"
        )
    ),
    SHARING_IS_CURRENTLY_UNAVAILABLE(
        resourceId = R.string.sharing_is_currently_unavailable,
        translations = mapOf(
            "en" to "Sharing is currently unavailable",
            "cy" to "Nid yw rhannu ar gael ar hyn o bryd"
        )
    ),
    HOW_LONG_THEY_WILL_KEEP_YOUR_DETAILS(
        resourceId = R.string.how_long_they_will_keep_your_details,
        translations = mapOf(
            "en" to "how long they will keep your details",
            "cy" to "am faint o amser y byddant yn cadw eich manylion"
        )
    ),
    HOW_YOUR_DETAILS_CAN_BE_USED(
        resourceId = R.string.how_your_details_can_be_used,
        translations = mapOf(
            "en" to "how your details can be used",
            "cy" to "sut y gall eich manylion gael ei ddefnyddio"
        )
    ),
    KEEP_YOUR_DETAILS_ONLY_AS_LONG_AS_NEEDED(
        resourceId = R.string.keep_your_details_only_as_long_as_needed,
        translations = mapOf(
            "en" to "keep your details only as long as they need to",
            "cy" to "cadw eich manylion ond cyhyd ag y mae angen"
        )
    ),
    NEVER_USE_YOUR_DETAILS_FOR_MARKETING_PURPOSES(
        resourceId = R.string.never_use_your_details_for_marketing_purposes,
        translations = mapOf(
            "en" to "never use your details for marketing purposes",
            "cy" to "peidio byth â defnyddio’ch manylion at ddibenion marchnata"
        )
    ),
    USE_ENCRYPTION_WHEN_STORING_YOUR_DETAILS(
        resourceId = R.string.use_encryption_when_storing_your_details,
        translations = mapOf(
            "en" to "use encryption when storing your details",
            "cy" to "ddefnyddio amgryptio wrth storio’ch manylion"
        )
    ),
    TURN_ON_BLUETOOTH(
        resourceId = R.string.turn_on_bluetooth,
        translations = mapOf(
            "en" to "Turn on Bluetooth",
            "cy" to "Troi Bluetooth ymlaen"
        )
    ),
    STOP_CHECK_DIALOG_TITLE(
        resourceId = R.string.stop_check_dialog_title,
        translations = mapOf(
            "en" to "Are you sure you want to stop the check?",
            "cy" to "Ydych chi’n siŵr eich bod eisiau stopio’r gwiriad?"
        )
    ),
    STOP_CHECK_DIALOG_BODY(
        resourceId = R.string.stop_check_dialog_body,
        translations = mapOf(
            "en" to "You’ll end your connection with the person doing the check. " +
                "Your details will not be shared.",
            "cy" to "Byddwch yn dod â’ch cysylltiad â’r person sy’n gwneud y gwiriad i ben. " +
                "Ni fydd eich manylion yn cael eu rhannu."
        )
    ),
    STOP_CHECK_DIALOG_STOP_BUTTON(
        resourceId = R.string.stop_check_dialog_stop_button,
        translations = mapOf(
            "en" to "Stop the check",
            "cy" to "Stopio’r gwiriad"
        )
    ),
    STOP_CHECK_DIALOG_CONTINUE_BUTTON(
        resourceId = R.string.stop_check_dialog_continue_button,
        translations = mapOf(
            "en" to "Continue with the check",
            "cy" to "Parhau gyda’r gwiriad"
        )
    ),
    TURN_ON_BLUETOOTH_BEFORE_SHARING(
        resourceId = R.string.turn_on_bluetooth_before_sharing,
        translations = mapOf(
            "en" to "Before you can share, you need to turn on Bluetooth.",
            "cy" to "Cyn y gallwch rannu, mae angen i chi droi Bluetooth ymlaen."
        )
    ),
    USE_QR_CODE_TO_CONNECT_NEXT(
        resourceId = R.string.use_qr_code_to_connect_next,
        translations = mapOf(
            "en" to "You will not connect with a device yet. You’ll use a QR code to do that next.",
            "cy" to
                "Ni fyddwch yn cysylltu â dyfais ar hyn o bryd. Byddwch yn defnyddio cod QR i wneud hynny nesaf."
        )
    ),
    YOU_MAY_BE_ABLE_TO_USE_A_PHYSICAL_DOCUMENT_INSTEAD(
        resourceId = R.string.you_may_be_able_to_use_a_physical_document_instead,
        translations = mapOf(
            "en" to "You may be able to use a physical document instead.",
            "cy" to "Efallai y gallwch ddefnyddio dogfen gorfforol yn lle hynny."
        )
    ),
    HOLDER_GETTING_DETAILS_LABEL(
        resourceId = R.string.holder_getting_details_label,
        translations = mapOf(
            "en" to "Getting list of details",
            "cy" to "Casglu rhestr o fanylion"
        )
    ),
    HOLDER_SHARING_DETAILS_LABEL(
        resourceId = R.string.holder_sharing_details_label,
        translations = mapOf(
            "en" to "Sharing your details",
            "cy" to "Rhannu eich manylion"
        )
    ),
    YOU_WILL_CONNECT_VIA_QR_LATER(
        resourceId = R.string.you_will_connect_via_qr_later,
        translations = mapOf(
            "en" to "You will not connect with a device yet. " +
                "You’ll use a QR code to do that later.",
            "cy" to "Ni fyddwch yn cysylltu â dyfais ar hyn o bryd. " +
                "Byddwch yn defnyddio cod QR i wneud hynny yn nes ymlaen."
        )
    ),
    YOU_WILL_NEED_TO_TURN_ON_BLUETOOTH(
        resourceId = R.string.you_will_need_to_turn_on_bluetooth,
        translations = mapOf(
            "en" to "If Bluetooth is turned off on your phone, " +
                "you’ll need to turn it on too.",
            "cy" to "Os yw Bluetooth wedi’i ddiffodd ar eich ffôn, " +
                "bydd angen i chi ei droi ymlaen hefyd."
        )
    ),
    YOUR_DETAILS_HAVE_NOT_BEEN_SHARED(
        resourceId = R.string.your_details_have_not_been_shared,
        translations = mapOf(
            "en" to "Your details have not been shared.",
            "cy" to "Nid yw eich manylion wedi’u rhannu."
        )
    )
    ;

    operator fun get(localeCode: String): String? = translations[localeCode]
}
