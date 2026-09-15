package uk.gov.onelogin.sharing.testapp.credential.attribute.select

enum class ReaderAuthOption(
    val displayName: String,
    val certificateChain: List<String>,
    val privateKeyChain: List<String>
) {
    VALID("Valid", leaf = "reader_valid_x509_leaf_certificate"),
    INVALID_NAME_CONSTRAINTS(
        "Invalid name constraints",
        leaf = "reader_x509_leaf_with_invalid_organisation"
    ),
    INVALID_MISSING_PRIVACY_POLICY(
        "Missing privacy policy URL",
        leaf = "reader_x509_leaf_without_privacy_policy"
    ),

    /** CI-provisioned via `POST /issue-reader-cert`; committed assets are placeholders. */
    DVS_DEV(
        displayName = "DVS (dev)",
        certificateChain = listOf("reader_dvs_dev_chain.der"),
        privateKeyChain = listOf("reader_dvs_dev_leaf_key.pem")
    ),

    /** CI-provisioned via `POST /issue-reader-cert`; committed assets are placeholders. */
    DVS_INTEGRATION(
        displayName = "DVS (integration)",
        certificateChain = listOf("reader_dvs_integration_chain.der"),
        privateKeyChain = listOf("reader_dvs_integration_leaf_key.pem")
    );

    // Mocked test options share a fixed upper chain and vary only the leaf,
    // with one cert (.der) and one key (.pem) file per member.
    constructor(displayName: String, leaf: String) : this(
        displayName = displayName,
        certificateChain = listOf(SHARED_ROOT, SHARED_INTERMEDIATE, leaf).map { "$it.der" },
        privateKeyChain = listOf(SHARED_ROOT, SHARED_INTERMEDIATE, leaf).map { "$it.pem" }
    )

    val leafCertificateAsset: String get() = certificateChain.last()

    private companion object {
        const val SHARED_ROOT = "test_reader_auth_x509_certificate"
        const val SHARED_INTERMEDIATE = "test_reader_auth_name_constrained_x509_certificate"
    }
}
