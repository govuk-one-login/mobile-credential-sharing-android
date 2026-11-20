package uk.gov.onelogin.sharing.verification.crl

/**
 * The host app's CRL configuration resource is missing or unusable.
 *
 * Thrown when the configuration is read, so the caller can stop before any download.
 */
sealed class CrlConfigurationException(message: String) : IllegalArgumentException(message) {
    /** `assets/credential-sharing-crl.json` was not found in the host app. */
    class MissingResource : CrlConfigurationException("Missing CRL configuration resource")

    /**
     * The resource exists but cannot be used: unreadable, not valid JSON, wrong schema or an
     * invalid URL. [reason] identifies the file or field for diagnostics.
     */
    class InvalidResource(val reason: String) : CrlConfigurationException(reason)
}