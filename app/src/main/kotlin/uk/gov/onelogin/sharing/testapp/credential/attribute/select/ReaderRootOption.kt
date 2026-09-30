package uk.gov.onelogin.sharing.testapp.credential.attribute.select

/**
 * The set of trusted ReaderAuth root options that the Test App can be configured with.
 */
enum class ReaderRootOption(val displayName: String) {
    SHARING_TEST_APP_MOCK(displayName = "Sharing Test App"),
    DVS_DEV(displayName = "DVS (dev)"),
    DVS_INTEGRATION(displayName = "DVS (integration)"),
    ALL(displayName = "All (Mock + DVS)")
}
