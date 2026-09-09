package uk.gov.onelogin.sharing.verification

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import java.security.cert.X509Certificate
import uk.gov.onelogin.sharing.verification.document.DocumentVerifier
import uk.gov.onelogin.sharing.verification.trust.TrustVerifier

@DependencyGraph(scope = CredentialVerificationScope::class)
interface CredentialVerificationGraph {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides trustedRootCertificate: X509Certificate): CredentialVerificationGraph
    }

    fun getDocumentVerifier(): DocumentVerifier

    fun getTrustVerifier(): TrustVerifier
}
