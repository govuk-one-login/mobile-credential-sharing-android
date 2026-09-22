package uk.gov.onelogin.sharing.testapp.credential

/**
 * Errors thrown by the Test App's failure-simulating
 * [uk.gov.onelogin.sharing.orchestration.CredentialProvider] implementations.
 * These exist only to make `sign()` failures reproducible in the Test App and are
 * not part of the Sharing SDK.
 */
sealed class MockSignException(message: String) : Exception(message) {

    /**
     * A fatal signing error: signing can never succeed for this credential.
     */
    class SignError : MockSignException("Mock fatal signing error")

    /**
     * The user cancelled the local-authentication prompt while signing.
     */
    class LocalAuthCancelled : MockSignException("Mock local authentication cancelled")
}
