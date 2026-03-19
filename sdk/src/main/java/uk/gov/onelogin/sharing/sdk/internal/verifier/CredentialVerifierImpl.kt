package uk.gov.onelogin.sharing.sdk.internal.verifier

import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import java.security.cert.Certificate
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.CredentialVerifier
import uk.gov.onelogin.sharing.sdk.api.verifier.ScannerGraph

class CredentialVerifierImpl(
    @Suppress("UnusedPrivateProperty")
    private val verificationRequest: VerificationRequest,
    @Suppress("UnusedPrivateProperty")
    private val trustedCertificates: List<Certificate>,
    override val orchestrator: Orchestrator.Verifier,
    override val appGraph: CredentialSharingAppGraph
) : CredentialVerifier {

    override val scannerViewModelFactory: MetroViewModelFactory by lazy {
        createGraphFactory<ScannerGraph.Factory>()
            .create(orchestrator)
            .metroViewModelFactory
    }
}
