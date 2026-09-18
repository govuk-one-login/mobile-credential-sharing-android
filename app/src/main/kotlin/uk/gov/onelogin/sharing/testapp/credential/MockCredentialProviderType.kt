package uk.gov.onelogin.sharing.testapp.credential

/**
 * Selects which [uk.gov.onelogin.sharing.orchestration.CredentialProvider] implementation the Test
 * App uses when a [MockCredential] is chosen from the Holder credential-selection screen.
 *
 * This exists purely to make `sign()` failures reproducible in the Test App without needing the
 * Wallet Core test app or a real local-authentication prompt.
 */
enum class MockCredentialProviderType {
    /** Normal Jane Doe: signs successfully every time. */
    NORMAL,

    /** Jane Doe (signing failure): every `sign()` call throws a fatal signing error. */
    SIGNING_FAILURE,

    /**
     * Jane Doe (authentication cancelled once): the first `sign()` call reports a local
     * authentication cancellation, then subsequent calls sign successfully.
     */
    AUTH_CANCELLED_ONCE
}
