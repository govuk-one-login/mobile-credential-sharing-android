package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import android.content.Context
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters
import uk.gov.onelogin.sharing.holder.R

@Suppress("SpreadOperator")
internal object MoreAboutDvsAnalyticsEvents {
    internal fun AnalyticsLogger.moreAboutDvsScreen(context: Context, vararg formatArgs: Any) =
        Screen(
            name = context.getEnglishString(
                R.string.more_about_dvs_title,
                *formatArgs.toList().toTypedArray()
            ),
            id = "288470f5-8bdb-43cf-8cd7-38bcd241413b",
            params = walletSharingRequiredParameters
        ).let(this::logEventV3Dot1)

    internal fun AnalyticsLogger.moreAboutDvsPrivacyPolicyButton(
        context: Context,
        vararg formatArgs: Any
    ) = Button(
        text = context.getEnglishString(
            R.string.more_about_dvs_privacy_policy_link,
            *formatArgs.toList().toTypedArray()
        ),
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)
}
