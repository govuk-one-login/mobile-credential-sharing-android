package uk.gov.onelogin.sharing.sdk.internal.verifier

import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.CredentialVerifier

class CredentialVerifierImpl(
    @Suppress("UnusedPrivateProperty")
    private val verificationRequest: VerificationRequest,
    override val orchestrator: Orchestrator.Verifier,
    override val appGraph: CredentialSharingAppGraph
) : CredentialVerifier
