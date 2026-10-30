package uk.gov.onelogin.sharing.holder.awaitingresolution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.launch
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.holder.awaitingresolution.AwaitingVerifierResolutionAnalytics.detailsSharedScreen
import uk.gov.onelogin.sharing.holder.awaitingresolution.AwaitingVerifierResolutionAnalytics.goBackToDocuments
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

    fun logScreenView() {
        analyticsLogger.detailsSharedScreen()
    }
    fun logGoBackToDocuments() {
        analyticsLogger.goBackToDocuments()
    }
}
