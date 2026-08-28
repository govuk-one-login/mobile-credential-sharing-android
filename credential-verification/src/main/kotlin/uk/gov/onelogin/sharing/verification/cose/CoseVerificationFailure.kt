package uk.gov.onelogin.sharing.verification.cose

/**
 * Sealed hierarchy for typed COSE verification failures.
 */
sealed class CoseVerificationFailure : Exception() {
    /** The COSE_Sign1 structure is invalid or malformed. */
    data object MalformedCoseSign1 : CoseVerificationFailure()

    /** The algorithm used in the COSE_Sign1 is not supported (only ES256 allowed). */
    data object UnsupportedAlgorithm : CoseVerificationFailure()

    /** The cryptographic signature could not be verified. */
    data object InvalidSignature : CoseVerificationFailure()

    /** The certificate chain is not trusted or invalid. */
    data object UntrustedCertificate : CoseVerificationFailure()

    /** A certificate in the chain has expired. */
    data object ExpiredCertificate : CoseVerificationFailure()

    /** The certificate does not match the required profile (e.g., Issuer vs Reader). */
    data class UnsupportedCertificateProfile(val reason: CertificateProfileReason) :
        CoseVerificationFailure()
}

/**
 * Stable reason identifiers for certificate profile validation failures.
 */
enum class CertificateProfileReason {
    INVALID_VALIDITY_PERIOD,
    INVALID_KEY_USAGE,
    INVALID_EXTENDED_KEY_USAGE,
    MISSING_CRITICAL_EXTENSION,
    UNSUPPORTED_ALGORITHM,
    CROSS_PURPOSE_REJECTION
}
