package uk.gov.onelogin.sharing.verification.document.models

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * @param signed When the MSO was signed.
 * @param validFrom When the credential becomes valid.
 * @param validUntil When the credential expires.
 * @param expectedUpdate When a credential update is expected. Defaults to null, meaning that it's
 * not known when a credential expects an update.
 */
@OptIn(ExperimentalTime::class)
data class ValidityInfo(
    val signed: Instant,
    val validFrom: Instant,
    val validUntil: Instant,
    val expectedUpdate: Instant? = null
)
