package uk.gov.onelogin.sharing.verification.trust.chain

import dev.zacsweers.metro.ContributesBinding
import java.security.cert.CertPathValidator
import java.security.cert.CertificateFactory
import java.security.cert.PKIXParameters
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.verification.CredentialVerificationScope
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult

@ContributesBinding(CredentialVerificationScope::class)
class CertificateChainValidatorImpl internal constructor(private val logger: Logger) :
    CertificateChainValidator {

    override fun verify(certificates: List<X509Certificate>, trustedRoot: X509Certificate) {
        try {
            val certFactory = CertificateFactory.getInstance("X.509")
            val certPath = certFactory.generateCertPath(certificates)

            val trustAnchor = TrustAnchor(trustedRoot, null)
            val params = PKIXParameters(setOf(trustAnchor)).apply {
                isRevocationEnabled = false
                addCertPathChecker(KeyUsageChecker(certificates.first()))
                addCertPathChecker(BasicConstraintsChecker(certificates.first()))
            }

            CertPathValidator.getInstance("PKIX").validate(certPath, params)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.error(LOG_TAG, "Certificate chain validation failed", e)
            throw VerificationResult.Failure(VerificationError.UNTRUSTED_CERTIFICATE)
        }
    }

    companion object {
        private val LOG_TAG = CertificateChainValidatorImpl::class.java.simpleName
    }
}
