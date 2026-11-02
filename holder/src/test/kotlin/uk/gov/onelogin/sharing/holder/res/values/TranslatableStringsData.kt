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
    ;

    operator fun get(localeCode: String): String? = translations[localeCode]
}
