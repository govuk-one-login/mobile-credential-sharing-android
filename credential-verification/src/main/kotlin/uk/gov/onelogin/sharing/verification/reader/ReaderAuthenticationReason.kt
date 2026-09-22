package uk.gov.onelogin.sharing.verification.reader

/**
 * Stable reason identifiers for Reader Authentication failures.
 */
enum class ReaderAuthenticationReason {
    /** The readerAuth field was missing from the DocRequest. */
    READER_AUTH_MISSING,

    /** The cryptographic COSE signature on Reader Authentication was invalid. */
    INVALID_READER_SIGNATURE,

    /** The COSE_Sign1 structure or x5chain header was malformed. */
    MALFORMED_READER_AUTH,

    /** The algorithm used in Reader Authentication is not supported. */
    UNSUPPORTED_READER_AUTH_ALGORITHM,

    /** The Reader certificate chain is untrusted, expired, or violates profile rules. */
    UNTRUSTED_READER_CERTIFICATE,

    /** The privacy-policy SIA extension entry or URL violates validation rules. */
    PRIVACY_POLICY_URL_INVALID,

    /** The decrypted DeviceRequest bytes could not be decoded. */
    MALFORMED_DEVICE_REQUEST
}
