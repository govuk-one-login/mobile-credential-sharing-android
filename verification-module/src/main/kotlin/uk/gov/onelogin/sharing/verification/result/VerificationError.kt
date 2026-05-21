package uk.gov.onelogin.sharing.verification.result

enum class VerificationError {
    /**
     * The SHA-256 hash of an IssuerSignedItem's raw bytes does not match the corresponding digest
     * in the MSO value digests map.
     */
    DIGEST_MISMATCH,

    /**
     * An IssuerSignedItem references a digest identifier that has no entry in the MSO value
     * digests map.
     */
    DIGEST_MISSING,

    /**
     * The deviceKeyInfo.deviceKey field in the MSO cannot be decoded as a valid EC P-256 public
     * key, or the requested namespaces fall outside the key's authorized scope.
     */
    INVALID_DEVICE_KEY,

    /**
     * The DeviceAuth COSE_Sign1 signature did not verify against the device public key extracted
     * from the MSO.
     */
    INVALID_DEVICE_SIGNATURE,

    /**
     * The MSO docType field does not match the expected document type, or does not match the
     * document's own docType.
     */
    INVALID_DOC_TYPE,

    /**
     * The IssuerAuth COSE_Sign1 signature did not verify against the issuer certificate's public
     * key.
     */
    INVALID_ISSUER_SIGNATURE,

    /**
     * The MSO version field is not "1.0".
     */
    INVALID_MSO_VERSION,

    /**
     * The IssuerAuth bytes cannot be decoded as a valid COSE_Sign1 structure.
     */
    MALFORMED_ISSUER_AUTH,

    /**
     * The MSO CBOR structure is malformed (missing required fields, unexpected types, or
     * structural violations).
     */
    MALFORMED_MSO,

    /**
     * The MSO digestAlgorithm field specifies an algorithm other than "SHA-256".
     */
    UNSUPPORTED_DIGEST_ALGORITHM,

    /**
     * Any certificate chain or path validation failure (unanchored chain, expired cert, bad
     * signature, constraint violation, CRL revocation, etc.).
     */
    UNTRUSTED_CERTIFICATE,

    /**
     * The MSO validFrom timestamp is in the future; the credential is not yet valid.
     */
    VALIDITY_FROM_OUT_OF_RANGE,

    /**
     * The MSO signed timestamp is in the future, or falls outside the issuer certificate's
     * validity window.
     */
    VALIDITY_SIGNED_OUT_OF_RANGE,

    /**
     * The MSO validUntil timestamp is in the past; the credential has expired.
     */
    VALIDITY_UNTIL_EXPIRED,

    /**
     * The MSO validUntil timestamp is after the issuer certificate's expiry; the credential would
     * outlive its issuing certificate.
     */
    VALIDITY_UNTIL_OUT_OF_RANGE
}
