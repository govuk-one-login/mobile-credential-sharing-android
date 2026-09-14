package uk.gov.onelogin.sharing.verification.cose.internal.profile

/**
 * Operation purpose for certificate profile validation.
 */
internal enum class CertificatePurpose {
    /** Issuer authentication for attached MSO verification. */
    ISSUER_AUTH,

    /** Reader authentication for detached reader verification. */
    READER_AUTH
}
