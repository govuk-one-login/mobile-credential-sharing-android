package uk.gov.onelogin.sharing.testapp.verifier

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest
import uk.gov.onelogin.sharing.sdk.api.verifier.VerificationSession

/**
 * ViewModel that caches the [VerificationSession] so it survives configuration changes.
 */
class VerifierJourneyViewModel(private val dispatcher: CoroutineDispatcher = Dispatchers.Default) :
    ViewModel() {
    private val _session = MutableStateFlow<VerificationSession?>(null)
    val session: StateFlow<VerificationSession?> = _session

    /**
     * Initialises the session if not already created.
     */
    fun getSession(
        context: Context,
        request: VerificationRequest,
        factory: (Context, VerificationRequest) -> VerificationSession
    ) {
        if (_session.value != null) return

        viewModelScope.launch {
            val result = withContext(dispatcher) {
                factory(context, request)
            }
            _session.value = result
        }
    }
}
