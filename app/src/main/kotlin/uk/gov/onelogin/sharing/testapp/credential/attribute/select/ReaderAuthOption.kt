package uk.gov.onelogin.sharing.testapp.credential.attribute.select

enum class ReaderAuthOption(val leafCertificateAssetFileName: String, val displayName: String) {
    VALID(
        leafCertificateAssetFileName = "reader_valid_x509_leaf_certificate.der",
        displayName = "Valid"
    ),
    INVALID_NAME_CONSTRAINTS(
        leafCertificateAssetFileName = "reader_x509_leaf_with_invalid_organisation.der",
        displayName = "Invalid name constraints"
    ),
    INVALID_MISSING_PRIVACY_POLICY(
        leafCertificateAssetFileName = "reader_x509_leaf_without_privacy_policy.der",
        displayName = "Missing privacy policy URL"
    )
}
