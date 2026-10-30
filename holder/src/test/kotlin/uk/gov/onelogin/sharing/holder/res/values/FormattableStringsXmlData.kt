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
    MORE_ABOUT_DVS(
        translations = mapOf(
            "en" to "More about <Argument 1>",
            "cy" to "Mwy am <Argument 1>"
        ),
        resourceId = R.string.more_about_dvs,
        *(1..1).toPlaceholderStrings()
    );
}
