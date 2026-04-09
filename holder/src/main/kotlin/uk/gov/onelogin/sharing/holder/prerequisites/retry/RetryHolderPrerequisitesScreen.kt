package uk.gov.onelogin.sharing.holder.prerequisites.retry

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.prerequisites.contracts.PrerequisiteActionContract
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.ResolvePrerequisiteAction
import uk.gov.onelogin.sharing.holder.prerequisites.retry.RetryHolderPrerequisitesViewModel.NavigationEvent as ViewModelEvent

@Inject
@ContributesIntoMap(HolderUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class RetryHolderPrerequisitesViewModel(
    private val orchestrator: Orchestrator.Holder,
    private val resolver: ResolvePrerequisiteAction<HolderSessionState>,
    private val logger: Logger,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel(), ResolvePrerequisiteAction<HolderSessionState> by resolver {

    val navigationEvent: SharedFlow<NavigationEvent?> = orchestrator.holderSessionState
        .map { state ->
            when (state) {
                is HolderSessionState.PresentingEngagement -> NavigationEvent.PassedPrerequisites
                is HolderSessionState.Preflight -> {
                    if (state.missingPrerequisites.none(
                            MissingPrerequisiteV2::isRecoverable
                        )
                    ) {
                        NavigationEvent.UnrecoverableError
                    } else {
                        null
                    }
                }

                else -> null
            }
        }.shareIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Lazily
        )

    private val preflightState: StateFlow<HolderSessionState.Preflight?> = orchestrator
        .holderSessionState
        .map { it as? HolderSessionState.Preflight }
        .stateIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Eagerly,
            orchestrator.holderSessionState.value as? HolderSessionState.Preflight
        )

    val prerequisites: StateFlow<List<Prerequisite>?> = preflightState
        .map { it?.map(MissingPrerequisiteV2::prerequisite) }
        .stateIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Eagerly,
            (orchestrator.holderSessionState.value as? HolderSessionState.Preflight)
                ?.map(MissingPrerequisiteV2::prerequisite)
        )

    fun recheckPrerequisites() = viewModelScope.launch(dispatcher) {
        (orchestrator.holderSessionState.value as? HolderSessionState.Preflight)
            ?.onComplete()
    }

    sealed interface NavigationEvent {
        data object PassedPrerequisites : NavigationEvent
        data object UnrecoverableError : NavigationEvent
    }
}

@Composable
internal fun RetryHolderPrerequisitesScreen(
    modifier: Modifier = Modifier,
    viewModel: RetryHolderPrerequisitesViewModel = metroViewModel(),
    context: Context = LocalContext.current,
    contract: ActivityResultLauncher<PrerequisiteAction> = rememberLauncherForActivityResult(
        PrerequisiteActionContract,
    ) {
        viewModel.recheckPrerequisites()
        Toast.makeText(
            context,
            "Performed prerequisite action",
            Toast.LENGTH_SHORT
        ).show()
    },
    onPassPrerequisites: () -> Unit = {},
    onUnrecoverableError: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnPassPrerequisites by rememberUpdatedState(onPassPrerequisites)
    val currentOnUnrecoverableError by rememberUpdatedState(onUnrecoverableError)
    val missingPrerequisites: List<Prerequisite>? by viewModel
        .prerequisites
        .collectAsStateWithLifecycle()

//    LaunchedEffect(Unit) {
//
//    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    viewModel.navigationEvent.collect { event ->
                        when (event) {
                            is ViewModelEvent.PassedPrerequisites -> currentOnPassPrerequisites()
                            is ViewModelEvent.UnrecoverableError -> currentOnUnrecoverableError()
                            else -> {
                                // do nothing with null events
                            }
                        }
                    }
                }
                viewModel.recheckPrerequisites()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        missingPrerequisites?.let { prerequisites ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacingSingle)
            ) {
                Text("Additional actions required for:")
                prerequisites.forEach { prerequisite ->
                    Text(prerequisite.toString())
                }

                Spacer(Modifier.height(spacingSingle))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.resolve(contract)
                    }
                ) {
                    Text("Resolve actions")
                }
            }

        } ?: CircularProgressIndicator()
    }
}