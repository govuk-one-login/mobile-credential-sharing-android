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
import uk.gov.onelogin.sharing.sdk.api.verifier.CredentialVerifier

/**
 * ViewModel that caches the [CredentialVerifier] so it survives configuration changes.
 */
class VerifierJourneyViewModel(private val dispatcher: CoroutineDispatcher = Dispatchers.Default) :
    ViewModel() {
    private val _verifier = MutableStateFlow<CredentialVerifier?>(null)
    val verifier: StateFlow<CredentialVerifier?> = _verifier

    /**
     * Initialises the verifier if not already created.
     */
    fun getVerifier(
        context: Context,
        request: VerificationRequest,
        factory: (Context, VerificationRequest) -> CredentialVerifier
    ) {
        if (_verifier.value != null) return

        viewModelScope.launch {
            val result = withContext(dispatcher) {
                factory(context, request)
            }
            _verifier.value = result
        }
    }
}
