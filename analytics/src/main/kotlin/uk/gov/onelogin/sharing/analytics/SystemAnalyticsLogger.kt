package uk.gov.onelogin.sharing.analytics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import uk.gov.logging.api.analytics.AnalyticsEvent
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
import uk.gov.logging.api.v2.CrashLogger
import uk.gov.logging.api.v2.Logger
import uk.gov.logging.api.v2.errorKeys.ErrorKeys
import uk.gov.onelogin.sharing.core.logger.logTag

class SystemAnalyticsLogger @JvmOverloads constructor(
    private val logger: Logger,
    private val crashLogger: CrashLogger? = null,
    private val enabled: MutableStateFlow<Boolean> = MutableStateFlow(true)
) : AnalyticsLogger {

    val isEnabled: StateFlow<Boolean> = enabled

    override fun logEvent(shouldLogEvent: Boolean, vararg events: AnalyticsEvent) {
        try {
            if (shouldLogEvent && isEnabled.value) {
                events
                    .map(::toLogMessage)
                    .forEach {
                        logger.debug(
                            logTag,
                            it
                        )
                    }
            } else {
                logger.debug(
                    logTag,
                    "Received an event that shouldn't be logged!"
                )
            }
        } catch (exception: Exception) {
            crashLogger?.log(
                exception,
                ErrorKeys.StringKey("analytics_event", events.toList().toString())
            )
        }
    }

    override fun setEnabled(isEnabled: Boolean) {
        this.enabled.value = isEnabled
    }

    private fun toLogMessage(event: AnalyticsEvent): String = "Received analytics event: $event"
}
