package uk.gov.onelogin.sharing.verifier.verify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.plus
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

@Inject
@ViewModelKey
@ContributesIntoMap(VerifierUiScope::class)
@OptIn(ExperimentalPermissionsApi::class)
class VerifierPrerequisitesViewModel(
    private val logger: Logger,
    orchestrator: Orchestrator.Verifier,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {
    val events: SharedFlow<VerifyCredentialEvents?> = orchestrator.verifierSessionState
        .map { session ->
            when (session) {
                is VerifierSessionState.Preflight -> VerifyCredentialEvents.NavigateToPreflight

                is VerifierSessionState.ReadyToScan -> VerifyCredentialEvents.NavigateToScanner

                is VerifierSessionState.Complete.Failed ->
                    VerifyCredentialEvents.NavigateToUnrecoverableError

                else -> null
            }.also {
                logger.debug(
                    logTag,
                    "Converted session state to navigation event: $it"
                )
            }
        }.shareIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Lazily
        )

    init {
        orchestrator.start()
    }
}

sealed interface VerifyCredentialEvents {
    data object NavigateToScanner : VerifyCredentialEvents
    data object NavigateToPreflight : VerifyCredentialEvents
    data object NavigateToUnrecoverableError : VerifyCredentialEvents
}
