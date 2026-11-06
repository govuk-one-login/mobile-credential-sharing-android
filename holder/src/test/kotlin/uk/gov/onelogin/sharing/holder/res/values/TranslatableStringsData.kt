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
    DETAILS_SHARED(
        resourceId = R.string.details_shared,
        translations = mapOf(
            "en" to "Details shared",
            "cy" to "Manylion wedi’u rhannu"
        )
    ),
    FIND_OUT_HOW_SHARING_WORKS(
        resourceId = R.string.find_out_how_sharing_works,
        translations = mapOf(
            "en" to "Find out how sharing works",
            "cy" to "Darganfyddwch sut mae rhannu yn gweithio"
        )
    ),
    GET_READY_TO_SHARE(
        resourceId = R.string.get_ready_to_share,
        translations = mapOf(
            "en" to "Get ready to share",
            "cy" to "Paratoi i rannu"
        )
    ),
    TURN_ON_BLUETOOTH(
        resourceId = R.string.turn_on_bluetooth,
        translations = mapOf(
            "en" to "Turn on Bluetooth",
            "cy" to "Troi Bluetooth ymlaen"
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
    GO_BACK_TO_DOCUMENTS(
        resourceId = R.string.go_back_to_documents,
        translations = mapOf(
            "en" to "Go back to documents",
            "cy" to "Ewch yn ôl i dogfennau"
        )
    ),
    PERSON_WILL_CHECK_DETAILS_SHARED(
        resourceId = R.string.person_will_check_details_shared,
        translations = mapOf(
            "en" to
                "The person will check the details you shared.\n\n" +
                "Your bluetooth connection with them will now end.",
            "cy" to
                "Bydd y person yn gwirio’r manylion rydych wedi’u rhannu.\n\n" +
                "Bydd eich cysylltiad Bluetooth gyda hwy nawr yn dod i ben."
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
    )
    ;

    operator fun get(localeCode: String): String? = translations[localeCode]
}
