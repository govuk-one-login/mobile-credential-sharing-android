package uk.gov.onelogin.sharing.testapp.credential.attribute.select

/**
 * Trusted ReaderAuth root certificate options that the Holder test app can be configured with.
 */
enum class ReaderRootOption(val displayName: String) {
    SHARING_TEST_APP_MOCK("Sharing Test App Mock Root"),
    DVS_DEV("DVS (dev) Root"),
    DVS_INTEGRATION("DVS (integration) Root"),
    ALL("All Trusted Reader Roots")
}
