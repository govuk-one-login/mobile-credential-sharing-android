package uk.gov.onelogin.sharing.verification

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * @param notBefore The leaf certificate's start of validity.
 * @param notAfter The leaf certificate's end of validity.
 */
@OptIn(ExperimentalTime::class)
data class CertificateValidityPeriod(
    val notBefore: Instant,
    val notAfter: Instant,
)
