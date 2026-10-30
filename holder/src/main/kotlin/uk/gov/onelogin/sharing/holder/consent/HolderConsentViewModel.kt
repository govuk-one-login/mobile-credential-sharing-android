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
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.holder.consent.HolderConsentAnalyticsEvents.agreeAndShareButton
import uk.gov.onelogin.sharing.holder.consent.HolderConsentAnalyticsEvents.cancelAndGoBackButton
import uk.gov.onelogin.sharing.holder.consent.HolderConsentAnalyticsEvents.consentScreen
import uk.gov.onelogin.sharing.holder.consent.HolderConsentAnalyticsEvents.readPrivacyNoticeButton
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentNamespace
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class HolderConsentViewModel(
    private val orchestrator: Orchestrator.Holder,
    private val analyticsLogger: AnalyticsLogger,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    val presentation: StateFlow<HolderConsentUiState?> = orchestrator
        .holderSessionState
        .map { state ->
            state as? HolderSessionState.AwaitingUserConsent
        }.map { consentState ->
            consentState?.presentation
        }.map { presentation ->
            presentation?.let {
                HolderConsentUiState(
                    privacyPolicyUrl = it.privacyPolicyUrl,
                    card = it.documents.firstOrNull()?.let { document ->
                        ConsentCard.from(
                            ConsentAttributeResolver.toDisplayAttributes(
                                document.namespaces.flatMap(ConsentNamespace::attributes)
                            )
                        )
                    },
                    normalisedOrganizationName = it.normalisedOrganizationName,
                    isBusy = it.isBusy
                )
            }
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

    fun sendScreenEvent() = viewModelScope.launch {
        analyticsLogger.consentScreen()
    }

    fun onReadPrivacyNotice() = viewModelScope.launch {
        analyticsLogger.readPrivacyNoticeButton()
    }

    fun onAccept() = viewModelScope.launch {
        analyticsLogger.agreeAndShareButton()
        orchestrator.confirmConsent()
    }

    fun onCancelAndGoBack() = viewModelScope.launch {
        analyticsLogger.cancelAndGoBackButton()
    }

    fun onDeny() = viewModelScope.launch {
        orchestrator.denyConsent()
    }
}
