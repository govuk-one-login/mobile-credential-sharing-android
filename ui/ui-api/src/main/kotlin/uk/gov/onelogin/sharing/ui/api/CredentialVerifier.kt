package uk.gov.onelogin.sharing.ui.api

import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

/**
 * Verifier role: Requests and verifies credentials from holders.
 */
interface CredentialVerifier {
    val appGraph: CredentialSharingAppGraph
}

data class VerificationRequest(val documentType: String, val requestedElements: List<String>)
