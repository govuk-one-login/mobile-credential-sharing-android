package uk.gov.onelogin.sharing.verifier.scan

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
import kotlinx.coroutines.plus
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.verifier.scan.state.VerifierUiState

@ContributesIntoMap(VerifierUiScope::class, binding = binding<ViewModel>())
@Inject
@ViewModelKey
class VerifierScannerViewModel(
    val orchestrator: Orchestrator.Verifier,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {
    val navigationEvents: SharedFlow<VerifierNavigationEvents> = orchestrator
        .verifierSessionState
        .mapNotNull { state ->
            when (state) {
                is VerifierSessionState.Complete.Failed -> {
                    VerifierNavigationEvents.NavigateToInvalidScreen(state.reason)
                }

                is VerifierSessionState.Connecting ->
                    VerifierNavigationEvents.NavigateToDiagnostic

                else -> null
            }
        }.distinctUntilChanged()
        .flowOn(dispatcher)
        .shareIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Lazily
        )

    val uiState: StateFlow<VerifierUiState> = orchestrator
        .verifierSessionState
        .map { state ->
            if (VerifierSessionState.ReadyToScan == state) {
                VerifierUiState.StartScanner
            } else {
                VerifierUiState.Loading
            }
        }.distinctUntilChanged()
        .flowOn(dispatcher)
        .stateIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Eagerly,
            VerifierUiState.Loading
        )
}
