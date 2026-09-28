package uk.gov.onelogin.sharing.holder.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentation
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class HolderConsentViewModel(
    private val orchestrator: Orchestrator.Holder,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {
    val presentation: StateFlow<ConsentPresentation?> = orchestrator
        .holderSessionState
        .map { state ->
            state as? HolderSessionState.AwaitingUserConsent
        }.map { consentState ->
            consentState?.presentation
        }.stateIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Eagerly,
            null
        )

    // Stores whether privacy policy is shown and survives configuration changes
    private val _showPrivacyPolicy = MutableStateFlow(false)
    val showPrivacyPolicy: StateFlow<Boolean> = _showPrivacyPolicy

    fun onShowPrivacyPolicy() {
        _showPrivacyPolicy.value = true
    }

    fun onClosePrivacyPolicy() {
        _showPrivacyPolicy.value = false
    }

    fun onAccept() = viewModelScope.launch(dispatcher) {
        orchestrator.confirmConsent()
    }

    fun onDeny() = viewModelScope.launch(dispatcher) {
        orchestrator.denyConsent()
    }
}
