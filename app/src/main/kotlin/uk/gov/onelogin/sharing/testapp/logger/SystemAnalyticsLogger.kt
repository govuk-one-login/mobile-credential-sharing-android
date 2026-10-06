package uk.gov.onelogin.sharing.testapp.logger

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import uk.gov.logging.api.analytics.AnalyticsEvent
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag

class SystemAnalyticsLogger(
    private val logger: Logger
) : AnalyticsLogger {
    val isEnabled = MutableStateFlow(true)

    override fun logEvent(shouldLogEvent: Boolean, vararg events: AnalyticsEvent) {
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
    }

    override fun setEnabled(isEnabled: Boolean) {
        this.isEnabled.update { isEnabled }
    }

    private fun toLogMessage(event: AnalyticsEvent): String =
        "Received analytics event: $event"
}