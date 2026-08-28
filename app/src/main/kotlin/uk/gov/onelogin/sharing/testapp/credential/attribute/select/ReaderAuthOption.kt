package uk.gov.onelogin.sharing.testapp.credential.attribute.select

enum class ReaderAuthOption(internal val leafCertificateAsset: String, val displayName: String) {
    VALID(
        leafCertificateAsset = "reader_valid_x509_leaf_certificate",
        displayName = "Valid"
    ),
    INVALID_NAME_CONSTRAINTS(
        leafCertificateAsset = "reader_x509_leaf_with_invalid_organisation",
        displayName = "Invalid name constraints"
    ),
    INVALID_MISSING_PRIVACY_POLICY(
        leafCertificateAsset = "reader_x509_leaf_without_privacy_policy",
        displayName = "Missing privacy policy URL"
    );

    private val assetChain: List<String> = listOf(
        "test_reader_auth_x509_certificate",
        "test_reader_auth_name_constrained_x509_certificate",
        leafCertificateAsset
    )

    val certificateChain: List<String> = assetChain.map { "$it.der" }
    val privateKeyChain: List<String> = assetChain.map { "$it.pem" }
}
