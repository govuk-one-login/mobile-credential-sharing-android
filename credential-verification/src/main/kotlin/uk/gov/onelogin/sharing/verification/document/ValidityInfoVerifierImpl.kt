package uk.gov.onelogin.sharing.verification.document

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import uk.gov.onelogin.sharing.verification.CredentialVerificationScope
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.validity.CertificateValidityPeriod
import uk.gov.onelogin.sharing.verification.format.document.validity.ValidityInfo

@OptIn(ExperimentalTime::class)
@Inject
@ContributesBinding(
    scope = CredentialVerificationScope::class,
    binding = binding<ValidityInfoVerifier>()
)
class ValidityInfoVerifierImpl(private val clock: Clock = Clock.System) : ValidityInfoVerifier {

    override fun verify(validityPeriod: CertificateValidityPeriod, validityInfo: ValidityInfo) {
        val now = clock.now()

        if (validityInfo.validUntil <= validityInfo.validFrom) {
            throw VerificationResult.Failure(VerificationError.MALFORMED_MSO)
        }

        if (validityInfo.signed > now ||
            validityInfo.signed < validityPeriod.notBefore ||
            validityInfo.signed > validityPeriod.notAfter
        ) {
            throw VerificationResult.Failure(VerificationError.VALIDITY_SIGNED_OUT_OF_RANGE)
        }

        if (validityInfo.validFrom < validityInfo.signed ||
            validityInfo.validFrom > now
        ) {
            throw VerificationResult.Failure(VerificationError.VALIDITY_FROM_OUT_OF_RANGE)
        }

        if (validityInfo.validUntil < now) {
            throw VerificationResult.Failure(VerificationError.VALIDITY_UNTIL_EXPIRED)
        }

        if (validityInfo.validUntil > validityPeriod.notAfter) {
            throw VerificationResult.Failure(VerificationError.VALIDITY_UNTIL_OUT_OF_RANGE)
        }
    }
}
