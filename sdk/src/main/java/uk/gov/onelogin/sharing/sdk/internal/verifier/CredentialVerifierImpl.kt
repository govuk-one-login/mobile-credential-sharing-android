package uk.gov.onelogin.sharing.sdk.internal.verifier

import java.security.cert.Certificate
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.CredentialVerifier

class CredentialVerifierImpl(
    @Suppress("UnusedPrivateProperty")
    private val verificationRequest: VerificationRequest,
    @Suppress("UnusedPrivateProperty")
    private val trustedCertificates: List<Certificate>,
    override val orchestrator: Orchestrator.Verifier,
    override val appGraph: CredentialSharingAppGraph
) : CredentialVerifier
