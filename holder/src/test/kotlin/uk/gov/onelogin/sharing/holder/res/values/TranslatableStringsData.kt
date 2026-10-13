package uk.gov.onelogin.sharing.holder.res.values

import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.holder.R

enum class TranslatableStringsData(
    private val translations: Map<String, String>,
    @param:StringRes val resourceId: Int
) {
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