package uk.gov.onelogin.sharing.verifier.scan

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator

@ContributesIntoMap(VerifierUiScope::class, binding = binding<ViewModel>())
@Inject
@ViewModelKey
class VerifierScannerViewModel(val orchestrator: Orchestrator.Verifier) : ViewModel()
