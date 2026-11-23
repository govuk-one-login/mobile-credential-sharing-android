package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import android.content.Context
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.buttonEvent
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.screenEvent
import uk.gov.onelogin.sharing.holder.R

/**
 * Analytics identities for the Turn on Bluetooth screen.
 *
 * Co-located with the screen it represents so that screen/button identity (names and IDs)
 * is owned by this feature module
 */
internal object TurnOnBluetoothAnalytics {

    internal fun AnalyticsLogger.turnOnBluetoothScreen(context: Context) = screenEvent(
        title = context.getEnglishString(R.string.holder_turn_on_bluetooth_title),
        id = "d64962b9-def5-4b13-9c3c-128d51353fe7"
    )

    internal fun AnalyticsLogger.turnOnBluetoothButton(context: Context) = buttonEvent(
        text = context.getEnglishString(R.string.holder_turn_on_bluetooth_button)
    )
}
