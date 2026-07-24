package uk.gov.onelogin.sharing.verifier.finish

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator

@Inject
@ContributesIntoMap(VerifierUiScope::class, binding = binding<ViewModel>())
@ViewModelKey
class FinishedVerifierJourneyViewModel(private val orchestrator: Orchestrator.Verifier) :
    ViewModel(),
    Resettable by orchestrator
