package uk.gov.onelogin.sharing.holder.awaitingresolution

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.holder.awaitingresolution.AwaitingVerifierResolutionAnalytics.detailsSharedScreen
import uk.gov.onelogin.sharing.holder.awaitingresolution.AwaitingVerifierResolutionAnalytics.goBackToDocuments
import uk.gov.onelogin.sharing.orchestration.Orchestrator

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class AwaitingVerifierResolutionViewModel(
    private val analyticsLogger: AnalyticsLogger,
    private val orchestrator: Orchestrator.Holder
) : ViewModel() {

    fun dismissJourney() {
        orchestrator.dismissJourney()
    }

    fun sendScreenEvent(screenName: String) {
        analyticsLogger.detailsSharedScreen(screenName)
    }
    fun sendGoBackToDocumentsEvent() {
        analyticsLogger.goBackToDocuments()
    }
}
