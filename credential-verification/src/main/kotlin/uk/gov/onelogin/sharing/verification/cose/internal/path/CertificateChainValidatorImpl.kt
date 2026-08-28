package uk.gov.onelogin.sharing.verification.cose.internal.path

import dev.zacsweers.metro.ContributesBinding
import java.security.cert.CertPathValidator
import java.security.cert.CertificateFactory
import java.security.cert.PKIXParameters
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate
import uk.gov.onelogin.sharing.verification.CredentialVerificationScope
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.UntrustedCertificate

@ContributesBinding(CredentialVerificationScope::class)
class CertificateChainValidatorImpl internal constructor() :
    CertificateChainValidator {

    override fun verify(certificates: List<X509Certificate>, trustedRoot: X509Certificate) {
        val result = try {
            val certFactory = CertificateFactory.getInstance("X.509")
            val certPath = certFactory.generateCertPath(certificates)

            val trustAnchor = TrustAnchor(trustedRoot, null)
            val params = PKIXParameters(setOf(trustAnchor)).apply {
                isRevocationEnabled = false
                addCertPathChecker(KeyUsageChecker(certificates.first()))
                addCertPathChecker(BasicConstraintsChecker(certificates.first()))
                addCertPathChecker(CertificateStructureChecker(certificates, trustedRoot))
                addCertPathChecker(IacaContentChecker(certificates, trustedRoot))
            }

            CertPathValidator.getInstance("PKIX").validate(certPath, params)
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            null
        }

        if (result == null) {
            throw UntrustedCertificate
        }
    }
}
