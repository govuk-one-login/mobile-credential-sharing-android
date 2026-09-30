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
    ),

    /** Local mock DVS 4-certificate chain using P-256 (ECDSA w/ SHA-256). */
    DVS_P256(
        displayName = "DVS P-256 (4 certs)",
        certificateChain = listOf("reader_dvs_p256_chain.der"),
        privateKeyChain = listOf("reader_dvs_p256_leaf_key.pem")
    ),

    /** Local mock DVS 4-certificate chain using P-384 (ECDSA w/ SHA-384). */
    DVS_P384(
        displayName = "DVS P-384 (4 certs)",
        certificateChain = listOf("reader_dvs_p384_chain.der"),
        privateKeyChain = listOf("reader_dvs_p384_leaf_key.pem")
    ),

    /** Local mock DVS 4-certificate hybrid chain (P-384 CA + P-256 leaf signed with SHA-256). */
    DVS_HYBRID(
        displayName = "DVS Hybrid (P-384 CA + P-256 Leaf)",
        certificateChain = listOf("reader_dvs_hybrid_chain.der"),
        privateKeyChain = listOf("reader_dvs_hybrid_leaf_key.pem")
    ),

    /** Mock 4-certificate chain with P-384 Leaf certified by P-256 CAs (SHA-256). */
    DVS_P384_LEAF_P256_CA(
        displayName = "DVS P-384 Leaf (P-256 CA)",
        certificateChain = listOf("reader_dvs_p384_leaf_p256_ca_chain.der"),
        privateKeyChain = listOf("reader_dvs_p384_leaf_p256_ca_leaf_key.pem")
    ),

    /** Mock 4-certificate chain with P-384 Leaf certified by P-256 CAs without privacy policy URL. */
    DVS_P384_LEAF_P256_CA_NO_POLICY(
        displayName = "DVS P-384 Leaf / P-256 CA (No Privacy Policy)",
        certificateChain = listOf("reader_dvs_p384_leaf_p256_ca_no_policy_chain.der"),
        privateKeyChain = listOf("reader_dvs_p384_leaf_p256_ca_no_policy_leaf_key.pem")
    ),

    /** Invalid 4-certificate hybrid chain (P-384 CA + P-256 Leaf) missing required ExtendedKeyUsage. */
    DVS_HYBRID_UNSUPPORTED(
        displayName = "DVS Hybrid Unsupported (P-384 CA + P-256 Leaf)",
        certificateChain = listOf("reader_dvs_hybrid_unsupported_chain.der"),
        privateKeyChain = listOf("reader_dvs_hybrid_unsupported_leaf_key.pem")
    ),

    /** Invalid 4-certificate chain with unsupported P-521 curve CA. */
    DVS_INVALID_CURVE(
        displayName = "DVS Invalid Curve (P-521 CA)",
        certificateChain = listOf("reader_dvs_invalid_curve_chain.der"),
        privateKeyChain = listOf("reader_dvs_invalid_curve_leaf_key.pem")
    );

    // Mocked test options share a fixed upper chain and vary only the leaf,
    // with one cert (.der) and one key (.pem) file per member.
    constructor(displayName: String, leaf: String) : this(
        displayName = displayName,
        certificateChain = listOf(leaf, SHARED_INTERMEDIATE, SHARED_ROOT).map { "$it.der" },
        privateKeyChain = listOf(leaf, SHARED_INTERMEDIATE, SHARED_ROOT).map { "$it.pem" }
    )

    val leafCertificateAsset: String get() = certificateChain.first()

    private companion object {
        const val SHARED_ROOT = "test_reader_auth_x509_certificate"
        const val SHARED_INTERMEDIATE = "test_reader_auth_name_constrained_x509_certificate"
    }
}
