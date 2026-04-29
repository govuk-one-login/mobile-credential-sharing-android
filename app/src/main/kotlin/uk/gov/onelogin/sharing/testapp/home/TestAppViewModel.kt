package uk.gov.onelogin.sharing.testapp.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class TestAppViewModel : ViewModel() {
    private val _events = MutableSharedFlow<NavigationEvent>()

    val events: SharedFlow<NavigationEvent> = _events

    fun update(event: NavigationEvent) = viewModelScope.launch {
        _events.emit(event)
    }

    sealed interface NavigationEvent {
        data object Holder : NavigationEvent
        data object Verifier : NavigationEvent
    }
}