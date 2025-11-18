package uk.gov.onelogin.sharing.testapp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import uk.gov.onelogin.sharing.testapp.destination.PrimaryTabDestination

class MainActivityViewModel : ViewModel() {
    private val _currentTabDestination = MutableStateFlow<PrimaryTabDestination>(
        PrimaryTabDestination.Holder
    )
    val currentTabDestination: StateFlow<PrimaryTabDestination> = _currentTabDestination

    fun update(destination: PrimaryTabDestination) {
        _currentTabDestination.update { destination }
    }
}
