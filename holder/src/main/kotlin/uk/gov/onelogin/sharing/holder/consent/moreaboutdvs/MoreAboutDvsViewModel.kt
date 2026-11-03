package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.launch
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.holder.consent.moreaboutdvs.MoreAboutDvsAnalyticsEvents.moreAboutDvsPrivacyPolicyButton
import uk.gov.onelogin.sharing.holder.consent.moreaboutdvs.MoreAboutDvsAnalyticsEvents.moreAboutDvsScreen

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class MoreAboutDvsViewModel(
    private val analyticsLogger: AnalyticsLogger,
) : ViewModel() {

    fun sendScreenEvent(context: Context) = viewModelScope.launch {
        analyticsLogger.moreAboutDvsScreen(context, "DVS")
    }

    fun sendButtonEvent(context: Context) = viewModelScope.launch {
        analyticsLogger.moreAboutDvsPrivacyPolicyButton(context, "DVS")
    }
}
