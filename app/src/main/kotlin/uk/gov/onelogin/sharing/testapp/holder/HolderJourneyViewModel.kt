package uk.gov.onelogin.sharing.testapp.holder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.gov.onelogin.sharing.sdk.api.presenter.SharingSession
import uk.gov.onelogin.sharing.testapp.credential.MockCredential

/**
 * ViewModel that caches the [SharingSession] so it survives configuration changes.
 */
class HolderJourneyViewModel(private val dispatcher: CoroutineDispatcher = Dispatchers.Default) :
    ViewModel() {
    private val _session = MutableStateFlow<SharingSession?>(null)
    val session: StateFlow<SharingSession?> = _session

    /**
     * Initialises the session if not already created.
     */
    fun getSession(credential: MockCredential, factory: (MockCredential) -> SharingSession) {
        if (_session.value != null) return

        viewModelScope.launch {
            val result = withContext(dispatcher) {
                factory(credential)
            }
            _session.value = result
        }
    }
}
