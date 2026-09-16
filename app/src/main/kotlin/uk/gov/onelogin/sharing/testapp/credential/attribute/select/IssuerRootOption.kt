package uk.gov.onelogin.sharing.testapp.credential.attribute.select

/**
 * The set of trusted IssuerAuth root certificates that the Verifier test app can be configured
 * with.
 *
 * Each option points to a single `.der` X.509 root certificate asset used as the trust anchor for
 * IssuerAuth verification via `VerifierConfig.trustedRootCertificate`.
 *
 */
enum class IssuerRootOption(internal val rootCertificateAsset: String, val displayName: String) {
    SHARING_TEST_APP_MOCK(
        rootCertificateAsset = "test_x509_certificate",
        displayName = "Sharing Test App"
    ),
    WALLET_CORE_BUILD(
        rootCertificateAsset = "wallet_core_root_x509_certificate",
        displayName = "Wallet (Dev/Build/Stage)"
    ),
    WALLET_CORE_INTEGRATION(
        rootCertificateAsset = "wallet_core_root_integration_x509_certificate",
        displayName = "Wallet (Integration)"
    );

    val certificateAsset: String = "$rootCertificateAsset.der"
}
