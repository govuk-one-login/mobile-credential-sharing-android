package uk.gov.onelogin.sharing.holder.res.values

import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.holder.R

/**
 * Test data used with [StringsXmlTest] for resource IDs that are translated into multiple locales.
 *
 * Each entry declares the expected value per locale (`"en"`, `"cy"`). Ensures tests fail when a
 * translated string changes, or a locale is added/removed, without updating this test data.
 */
enum class TranslatableStringsData(
    private val translations: Map<String, String>,
    @param:StringRes val resourceId: Int
) {
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
    );

    operator fun get(locale: String): String = requireNotNull(translations[locale]) {
        "Missing '$locale' translation for $name"
    }
}
