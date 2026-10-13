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
    )
    ;

    operator fun get(localeCode: String): String? = translations[localeCode]
}