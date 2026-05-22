package uk.gov.onelogin.sharing.verification.document.models

import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import uk.gov.onelogin.sharing.verification.document.models.CertificateValidityPeriodStubs.now

@OptIn(ExperimentalTime::class)
object CertificateValidityPeriodStubs {
    private val now = Clock.System.now()

    /**
     * @param instant The [Instant] used to shift timestamps. Defaults to [now].
     *
     * @return Active instance due to [CertificateValidityPeriod.notBefore] being in the past and
     * [CertificateValidityPeriod.notAfter] being in the future.
     */
    fun valid(instant: Instant = now) = CertificateValidityPeriod(
        notBefore = instant.minus(1.minutes),
        notAfter = instant.plus(1.minutes)
    )

    /**
     * @param instant The [Instant] used to shift timestamps. Defaults to [now].
     *
     * @return [CertificateValidityPeriod] instance with [CertificateValidityPeriod.notBefore]
     * being a later timestamp than [CertificateValidityPeriod.notAfter].
     */
    fun notBeforeLaterThanNotAfter(instant: Instant = now) = CertificateValidityPeriod(
        notAfter = instant.plus(1.minutes),
        notBefore = instant.plus(2.minutes),
    )

    /**
     * @param instant The [Instant] used to shift timestamps. Defaults to [now].
     *
     * @return [CertificateValidityPeriod] instance with both [CertificateValidityPeriod.notBefore]
     * and [CertificateValidityPeriod.notAfter] being in the past.
     */
    fun expired(instant: Instant = now) = CertificateValidityPeriod(
        notAfter = instant.minus(1.minutes),
        notBefore = instant.minus(2.minutes)
    )

    /**
     * @param instant The [Instant] used to shift timestamps. Defaults to [now].
     *
     * @return [CertificateValidityPeriod] where both [CertificateValidityPeriod.notBefore] and
     * [CertificateValidityPeriod.notAfter] are future timestamps.
     */
    fun inactive(instant: Instant = now) = CertificateValidityPeriod(
        notBefore = instant.plus(1.minutes),
        notAfter = instant.plus(2.minutes)
    )
}