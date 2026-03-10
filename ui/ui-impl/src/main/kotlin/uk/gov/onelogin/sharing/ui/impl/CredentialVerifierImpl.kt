package uk.gov.onelogin.sharing.ui.impl

import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.ui.api.CredentialVerifier
import uk.gov.onelogin.sharing.ui.api.VerificationRequest
import java.security.cert.Certificate

class CredentialVerifierImpl(
    @Suppress("UnusedPrivateProperty")
    private val verificationRequest: VerificationRequest,
    @Suppress("UnusedPrivateProperty")
    private val trustedCertificates: List<Certificate>,
    override val appGraph: CredentialSharingAppGraph
) : CredentialVerifier

