package uk.gov.onelogin.sharing.testapp

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import uk.gov.onelogin.sharing.testapp.destination.PrimaryTabDestination
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val message: String
) : ViewModel() {

    init {
        Log.d("TestMessageLog", message)
    }

    private val _currentTabDestination = MutableStateFlow<PrimaryTabDestination>(
        PrimaryTabDestination.Holder
    )
    val currentTabDestination: StateFlow<PrimaryTabDestination> = _currentTabDestination

    fun update(destination: PrimaryTabDestination) {
        _currentTabDestination.update { destination }
    }
}
