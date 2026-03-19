package uk.gov.onelogin.sharing.sdk

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.Provider
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import kotlin.reflect.KClass
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.CredentialVerifier

private val emptyFactory = object : MetroViewModelFactory() {
    override val viewModelProviders: Map<KClass<out ViewModel>, Provider<ViewModel>> = emptyMap()
    override val assistedFactoryProviders:
        Map<KClass<out ViewModel>, Provider<ViewModelAssistedFactory>> = emptyMap()
    override val manualAssistedFactoryProviders:
        Map<KClass<out ManualViewModelAssistedFactory>, Provider<ManualViewModelAssistedFactory>> =
        emptyMap()
}

class FakeCredentialVerifier(
    override val orchestrator: Orchestrator.Verifier,
    override val appGraph: CredentialSharingAppGraph,
    override val scannerViewModelFactory: MetroViewModelFactory = emptyFactory
) : CredentialVerifier
