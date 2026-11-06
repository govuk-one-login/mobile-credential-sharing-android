package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.BleRestrictedAnalytics.bleRestrictedGoBackButton
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.BleRestrictedAnalytics.bleRestrictedScreen
import uk.gov.onelogin.sharing.orchestration.Orchestrator

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class BleRestrictedViewModel(
    private val analyticsLogger: AnalyticsLogger,
    private val orchestrator: Orchestrator.Holder,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel(),
    Resettable by orchestrator {

    fun sendScreenEvent(screenName: String): Job = viewModelScope.launch(dispatcher) {
        analyticsLogger.bleRestrictedScreen(screenName)
    }

    fun sendGoBackEvent(buttonText: String): Job = viewModelScope.launch(dispatcher) {
        analyticsLogger.bleRestrictedGoBackButton(buttonText)
    }
}
