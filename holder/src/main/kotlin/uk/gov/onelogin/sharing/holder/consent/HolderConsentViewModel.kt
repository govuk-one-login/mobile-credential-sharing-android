package uk.gov.onelogin.sharing.holder.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
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
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentDocument
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentNamespace
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentation
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class HolderConsentViewModel(
    private val orchestrator: Orchestrator.Holder,
    private val analyticsLogger: AnalyticsLogger,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val showMoreAboutDvs = MutableStateFlow(false)

    private val presentation: Flow<ConsentPresentation> = orchestrator
        .holderSessionState
        .filterIsInstance<HolderSessionState.AwaitingUserConsent>()
        .map { it.presentation }

    val uiState: StateFlow<HolderConsentUiState> = combine(
        presentation,
        showMoreAboutDvs
    ) { presentation, showMoreAboutDvs ->
        if (showMoreAboutDvs) {
            HolderConsentUiState.MoreAboutDvs(
                normalisedOrganizationName = presentation.normalisedOrganizationName,
                privacyPolicyUrl = presentation.privacyPolicyUrl,
            )
        } else {
            presentation.firstDocumentAttributes
                ?.let(ConsentAttributeResolver::toDisplayAttributes)
                ?.let(ConsentCard::from)
            HolderConsentUiState.ConfirmConsent(
                privacyPolicyUrl = presentation.privacyPolicyUrl,
                card = presentation.documents.firstOrNull()
                    ?.let(ConsentDocument::namespaceAttributes)
                    ?.let(ConsentAttributeResolver::toDisplayAttributes)
                    ?.let(ConsentCard::from),
                normalisedOrganizationName = presentation.normalisedOrganizationName,
                organizationName = presentation.organizationName,
                isBusy = presentation.isBusy,
            )
        }
    }.stateIn(
        viewModelScope.plus(dispatcher),
        SharingStarted.Eagerly,
        HolderConsentUiState.Loading
    )

    fun sendScreenEvent(screenName: String) = viewModelScope.launch {
        analyticsLogger.consentScreen(screenName)
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
