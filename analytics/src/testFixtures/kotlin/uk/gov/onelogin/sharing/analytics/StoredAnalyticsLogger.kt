package uk.gov.onelogin.sharing.analytics

import kotlinx.coroutines.flow.MutableStateFlow
import uk.gov.logging.api.analytics.AnalyticsEvent
import uk.gov.logging.api.analytics.logging.AnalyticsLogger

class StoredAnalyticsLogger(
    private val subLogger: AnalyticsLogger? = null,
    private val isEnabledFlag: MutableStateFlow<Boolean> = MutableStateFlow(true)
) : AnalyticsLogger,
    Iterable<AnalyticsEvent> {
    private var _sentAnalyticsEvents = mutableListOf<AnalyticsEvent>()
    private var _ignoredAnalyticsEvents = mutableListOf<AnalyticsEvent>()

    val sentAnalyticsEvents: List<AnalyticsEvent> get() = _sentAnalyticsEvents
    val ignoredAnalyticsEvents: List<AnalyticsEvent> get() = _ignoredAnalyticsEvents

    init {
        setEnabled(isEnabledFlag.value)
    }

    @Suppress("SpreadOperator")
    override fun logEvent(shouldLogEvent: Boolean, vararg events: AnalyticsEvent) {
        if (shouldLogEvent && isEnabledFlag.value) {
            _sentAnalyticsEvents.addAll(events)
        } else {
            _ignoredAnalyticsEvents.addAll(events)
        }

        subLogger?.logEvent(shouldLogEvent, *events)
    }

    override fun setEnabled(isEnabled: Boolean) {
        isEnabledFlag.value = isEnabled
        subLogger?.setEnabled(isEnabled)
    }

    override fun iterator(): Iterator<AnalyticsEvent> = (
        sentAnalyticsEvents + ignoredAnalyticsEvents
        ).iterator()
}
