package uk.gov.onelogin.sharing.verifier.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.verifier.VerifierNavigationEvents
import uk.gov.onelogin.sharing.verifier.scan.state.VerifierUiState

@ContributesIntoMap(VerifierUiScope::class, binding = binding<ViewModel>())
@Inject
@ViewModelKey(VerifierScannerViewModel::class)
class VerifierScannerViewModel(private val orchestrator: Orchestrator.Verifier) : ViewModel() {

    private val _navigationEvents = MutableSharedFlow<VerifierNavigationEvents>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    private val _uiState = MutableStateFlow<VerifierUiState>(VerifierUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            orchestrator.verifierSessionState.collect {

                when (it) {

                    is VerifierSessionState.Complete.Failed -> {
                        _navigationEvents.emit(
                            VerifierNavigationEvents.NavigateToInvalidScreen(it.reason)
                        )
                        orchestrator.reset()
                    }

                    is VerifierSessionState.ProcessingEngagement -> _navigationEvents.emit(
                        VerifierNavigationEvents.NavigateToDiagnostic(it.qrCode)
                    )

                    is VerifierSessionState.ReadyToScan -> {
                        _uiState.emit(VerifierUiState.StartScanner)
                    }

                    else -> Unit
                }
            }
        }
    }
}
