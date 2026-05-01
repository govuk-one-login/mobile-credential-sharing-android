package uk.gov.onelogin.sharing.holder.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class HolderConsentViewModel(
    private val orchestrator: Orchestrator.Holder,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {
    val deviceRequest: StateFlow<DeviceRequest?> = orchestrator
        .holderSessionState
        .map { state ->
            state as? HolderSessionState.AwaitingUserConsent
        }.map { consentState ->
            consentState?.request
        }.stateIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Eagerly,
            null
        )

    val navEvents: SharedFlow<HolderConsentNavEvents> = orchestrator
        .holderSessionState
        .mapNotNull { state ->
            when (state) {
                is HolderSessionState.Complete.Failed ->
                    HolderConsentNavEvents.NavigateToGenericError

                else -> null
            }
        }.distinctUntilChanged()
        .flowOn(dispatcher)
        .shareIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Lazily
        )

    fun onAccept() = viewModelScope.launch(dispatcher) {
        orchestrator.confirmConsent()
    }

    fun onDeny() = viewModelScope.launch(dispatcher) {
        orchestrator.cancel()
    }
}
