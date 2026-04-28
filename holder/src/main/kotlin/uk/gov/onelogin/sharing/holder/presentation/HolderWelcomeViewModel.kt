package uk.gov.onelogin.sharing.holder.presentation

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
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestDecodingException
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.exceptions.BluetoothDisconnectedException
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class HolderWelcomeViewModel(
    private val logger: Logger,
    orchestrator: Orchestrator.Holder,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val navEvents: SharedFlow<HolderScreenEvents?> = orchestrator
        .holderSessionState
        .map(::convertToNavigationEvent)
        .distinctUntilChanged()
        .flowOn(dispatcher)
        .shareIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Lazily
        )

    val uiState: StateFlow<HolderWelcomeUiState> = orchestrator
        .holderSessionState
        .map { sessionState ->
            HolderWelcomeUiState(
                qrData = (sessionState as? HolderSessionState.PresentingEngagement)?.qrData
            )
        }.distinctUntilChanged()
        .flowOn(dispatcher)
        .stateIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Eagerly,
            HolderWelcomeUiState()
        )

    private fun convertToNavigationEvent(sessionState: HolderSessionState): HolderScreenEvents? =
        when (sessionState) {
            is HolderSessionState.Complete.Failed -> {
                val exception =
                    (sessionState.sessionReason as? SessionErrorReason.UnrecoverableThrowable)
                        ?.exception

                when (exception) {
                    is BluetoothDisconnectedException -> {
                        HolderScreenEvents.NavigateToBluetoothError(
                            BluetoothSessionError.BluetoothConnectionError
                        )
                    }

                    is DeviceRequestDecodingException -> {
                        HolderScreenEvents.NavigateToGenericError
                    }

                    else -> {
                        HolderScreenEvents.NavigateToGenericError
                    }
                }
            }

            is HolderSessionState.AwaitingUserConsent ->
                HolderScreenEvents.AwaitingUserContent

            else -> null
        }.also {
            logger.debug(
                logTag,
                "Updated navigation event: $it"
            )
        }
}

data class HolderWelcomeUiState(val qrData: String? = null)
