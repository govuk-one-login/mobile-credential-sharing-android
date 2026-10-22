package uk.gov.onelogin.sharing.holder.awaitingresolution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.launch
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.TrackEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.orchestration.Orchestrator

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class AwaitingVerifierResolutionViewModel(
    private val analyticsLogger: AnalyticsLogger,
    private val orchestrator: Orchestrator.Holder
) : ViewModel(),
    Resettable {

    override fun reset() {
        viewModelScope.launch { orchestrator.reset() }
    }

    fun logScreenView() = analyticsLogger.logEventV3Dot1(screenViewEvent)
    fun logGoBackToDocuments() = analyticsLogger.logEventV3Dot1(goBackToDocumentsEvent)

    companion object {
        internal val screenViewEvent = ViewEvent.Screen(
            name = "Details shared",
            id = "c2ccd487-c0e0-4ef8-943b-c555c4856a2c",
            params = walletSharingRequiredParameters
        )

        internal val goBackToDocumentsEvent = TrackEvent.Button(
            "Go back to documents",
            walletSharingRequiredParameters
        )
    }
}
