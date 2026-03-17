package uk.gov.onelogin.sharing.ui.impl

import java.security.cert.Certificate
import uk.gov.onelogin.sharing.sdk.di.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.ui.api.CredentialVerifier
import uk.gov.onelogin.sharing.ui.api.VerificationRequest

class CredentialVerifierImpl(
    @Suppress("UnusedPrivateProperty")
    private val verificationRequest: VerificationRequest,
    @Suppress("UnusedPrivateProperty")
    private val trustedCertificates: List<Certificate>,
    override val appGraph: CredentialSharingAppGraph
) : CredentialVerifier
