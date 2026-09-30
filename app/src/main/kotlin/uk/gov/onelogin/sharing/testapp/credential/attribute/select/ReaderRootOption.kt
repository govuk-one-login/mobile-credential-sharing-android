package uk.gov.onelogin.sharing.testapp.credential.attribute.select

/**
 * The set of trusted ReaderAuth root options that the Test App can be configured with.
 */
enum class ReaderRootOption(val displayName: String) {
    SHARING_TEST_APP_MOCK(displayName = "Sharing Test App"),
    DVS_DEV(displayName = "DVS (dev)"),
    DVS_INTEGRATION(displayName = "DVS (integration)"),
    DVS_P256(displayName = "DVS P-256 (4 certs)"),
    DVS_P384(displayName = "DVS P-384 (4 certs)"),
    DVS_HYBRID(displayName = "DVS Hybrid (P-384 CA)"),
    DVS_P384_LEAF_P256_CA(displayName = "DVS P-384 Leaf (P-256 CA)"),
    DVS_P384_LEAF_P256_CA_NO_POLICY(displayName = "DVS P-384 Leaf / P-256 CA (No Privacy Policy)"),
    DVS_HYBRID_UNSUPPORTED(displayName = "DVS Hybrid Unsupported (P-384 CA)"),
    DVS_INVALID_CURVE(displayName = "DVS Invalid Curve (P-521 CA)")
}
