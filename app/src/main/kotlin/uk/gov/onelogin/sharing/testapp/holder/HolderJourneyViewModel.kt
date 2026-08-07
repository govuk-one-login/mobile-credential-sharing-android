package uk.gov.onelogin.sharing.testapp.holder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.gov.onelogin.sharing.sdk.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.testapp.credential.MockCredential

/**
 * ViewModel that caches the [CredentialPresenter] so it survives configuration changes.
 */
class HolderJourneyViewModel(private val dispatcher: CoroutineDispatcher = Dispatchers.Default) :
    ViewModel() {
    private val _presenter = MutableStateFlow<CredentialPresenter?>(null)
    val presenter: StateFlow<CredentialPresenter?> = _presenter

    /**
     * Initialises the presenter if not already created.
     */
    fun getPresenter(credential: MockCredential, factory: (MockCredential) -> CredentialPresenter) {
        if (_presenter.value != null) return

        viewModelScope.launch {
            val result = withContext(dispatcher) {
                factory(credential)
            }
            _presenter.value = result
        }
    }
}
