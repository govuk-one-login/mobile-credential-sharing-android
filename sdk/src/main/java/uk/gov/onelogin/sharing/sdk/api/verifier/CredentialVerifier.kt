package uk.gov.onelogin.sharing.sdk.api.verifier

import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph

interface CredentialVerifier {
    val appGraph: CredentialSharingAppGraph

    val orchestrator: Orchestrator.Verifier

    val scannerViewModelFactory: MetroViewModelFactory
}
