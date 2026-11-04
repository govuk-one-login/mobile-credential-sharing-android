package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.holder.consent.moreaboutdvs.MoreAboutDvsAnalyticsEvents.moreAboutDvsPrivacyPolicyButton
import uk.gov.onelogin.sharing.holder.consent.moreaboutdvs.MoreAboutDvsAnalyticsEvents.moreAboutDvsScreen
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class MoreAboutDvsViewModel(
    private val analyticsLogger: AnalyticsLogger,
    orchestrator: Orchestrator.Holder
) : ViewModel() {

    val organizationName: StateFlow<String> = orchestrator.holderSessionState
        .filterIsInstance<HolderSessionState.AwaitingUserConsent>()
        .map { it.presentation }
        .mapNotNull { it.normalisedOrganizationName }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            "Unknown"
        )

    fun sendScreenEvent(context: Context) = viewModelScope.launch {
        analyticsLogger.moreAboutDvsScreen(context, "DVS")
    }

    fun sendButtonEvent(context: Context) = viewModelScope.launch {
        analyticsLogger.moreAboutDvsPrivacyPolicyButton(context, "DVS")
    }
}
