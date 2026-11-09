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
        ),
    ),
    SHARING_IS_CURRENTLY_UNAVAILABLE(
        resourceId = R.string.sharing_is_currently_unavailable,
        translations = mapOf(
            "en" to "Sharing is currently unavailable",
            "cy" to "Nid yw rhannu ar gael ar hyn o bryd"
        ),
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
    YOU_MAY_BE_ABLE_TO_USE_A_PHYSICAL_DOCUMENT_INSTEAD(
        resourceId = R.string.you_may_be_able_to_use_a_physical_document_instead,
        translations = mapOf(
            "en" to "You may be able to use a physical document instead.",
            "cy" to "Efallai y gallwch ddefnyddio dogfen gorfforol yn lle hynny."
        ),
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
        ),
    ),
    ;

    operator fun get(localeCode: String): String? = translations[localeCode]
}
