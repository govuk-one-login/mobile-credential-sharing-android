package uk.gov.onelogin.sharing.verification.crl

import java.io.ByteArrayInputStream
import java.security.cert.CRLException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509CRL

/** The signed validity window of a CRL, in epoch milliseconds. */
internal data class CrlValidity(val thisUpdate: Long, val nextUpdate: Long)

/** Seam for decoding CRL validity dates without coupling callers to [CertificateFactory]. */
internal fun interface CrlValidityDecoder {
    /** Null if [bytes] is not a DER X.509 CRL, or lacks `thisUpdate` / `nextUpdate`. */
    fun readValidity(bytes: ByteArray): CrlValidity?
}

/**
 * Reads structure and dates from DER-encoded CRL bytes only. It does not verify the signature,
 * issuer or coverage, so a result is not evidence that the CRL is trustworthy.
 */
internal object CrlDecoder : CrlValidityDecoder {

    override fun readValidity(bytes: ByteArray): CrlValidity? {
        val crl = try {
            CertificateFactory.getInstance("X.509")
                .generateCRL(ByteArrayInputStream(bytes)) as? X509CRL
        } catch (_: CRLException) {
            null
        } catch (_: CertificateException) {
            null
        } ?: return null

        val thisUpdate = crl.thisUpdate ?: return null
        val nextUpdate = crl.nextUpdate ?: return null
        return CrlValidity(thisUpdate = thisUpdate.time, nextUpdate = nextUpdate.time)
    }
}