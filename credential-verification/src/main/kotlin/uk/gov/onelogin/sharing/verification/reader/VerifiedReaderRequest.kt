package uk.gov.onelogin.sharing.verification.reader

import java.security.cert.X509Certificate
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest

/**
 * Result of successful cryptographic and certificate verification containing the validated [DocRequest]
 * and the verified Reader leaf certificate.
 */
data class VerifiedReaderRequest(val docRequest: DocRequest, val readerCertificate: X509Certificate)
