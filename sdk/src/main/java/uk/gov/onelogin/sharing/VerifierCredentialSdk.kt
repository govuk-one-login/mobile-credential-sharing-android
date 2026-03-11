package uk.gov.onelogin.sharing

import java.security.cert.Certificate
import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

fun interface VerifierCredentialSdk {
    fun verifier(): CredentialVerifierNew
}

interface CredentialVerifierNew {
    val appGraph: CredentialSharingAppGraph

    val orchestrator: Orchestrator.Verifier
}

class CredentialVerifierNewImpl(
    @Suppress("UnusedPrivateProperty")
    private val verificationRequest: VerificationRequestNew,
    @Suppress("UnusedPrivateProperty")
    private val trustedCertificates: List<Certificate>,
    override val orchestrator: Orchestrator.Verifier,
    override val appGraph: CredentialSharingAppGraph
) : CredentialVerifierNew

class VerificationRequestNew
