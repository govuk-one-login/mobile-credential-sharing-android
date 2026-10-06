package uk.gov.onelogin.sharing.testapp.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel2
import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel3
import uk.gov.logging.api.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.RequiredParameters
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@HiltViewModel
class TestAppViewModel(
    private val analyticsLogger: AnalyticsLogger,
    private val ioDispatcher: CoroutineContext,
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

    fun update(event: NavigationEvent) = viewModelScope.launch {
        _events.emit(event)
    }

    fun sendScreenEvent() {
        viewModelScope.launch(ioDispatcher) {
            // DCMAW-18130: Extract 'constants' into gradle module
            Screen(
                name = "Credential Sharing Test App",
                id = "TestAppScreen",
                params = RequiredParameters(
                    taxonomyLevel2 = TaxonomyLevel2.WALLET,
                )
            ).let(analyticsLogger::logEventV3Dot1)
        }
    }

    sealed interface NavigationEvent {
        data object Holder : NavigationEvent
        data object Verifier : NavigationEvent
    }
}
