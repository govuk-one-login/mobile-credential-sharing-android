package uk.gov.onelogin.sharing.testapp.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
import uk.gov.logging.api.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.TrackEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

@HiltViewModel
class TestAppViewModel(
    private val analyticsLogger: AnalyticsLogger,
    private val ioDispatcher: CoroutineContext
) : ViewModel() {
    private val _events = MutableSharedFlow<NavigationEvent>()

    val events: SharedFlow<NavigationEvent> = _events

    @Inject
    constructor(
        analyticsLogger: AnalyticsLogger
    ) : this(
        analyticsLogger = analyticsLogger,
        ioDispatcher = Dispatchers.IO
    )

    fun update(event: NavigationEvent) = viewModelScope.launch(ioDispatcher) {
        _events.emit(event)
        // DCMAW-18130: Disabled events for button click
        TrackEvent.Button(
            event.buttonName,
            walletSharingRequiredParameters
        ).let { analyticsEvent ->
            analyticsLogger.logEvent(
                shouldLogEvent = false,
                analyticsEvent.asLegacyEvent()
            )
        }
    }

    fun sendScreenEvent() = viewModelScope.launch(ioDispatcher) {
        Screen(
            name = "Credential Sharing Test App",
            id = "TestAppScreen",
            params = walletSharingRequiredParameters
        ).let(analyticsLogger::logEventV3Dot1)
    }

    sealed interface NavigationEvent {
        val buttonName: String

        data object Holder : NavigationEvent {
            override val buttonName: String get() = "Holder"
        }
        data object Verifier : NavigationEvent {
            override val buttonName: String get() = "Verifier"
        }
    }
}
