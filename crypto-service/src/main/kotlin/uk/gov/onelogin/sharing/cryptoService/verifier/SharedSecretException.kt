package uk.gov.onelogin.sharing.cryptoService.verifier

sealed class SharedSecretException(message: String, cause: Throwable? = null) :
    Exception(message, cause) {
    class IncompatibleCurve(curve: String) :
        SharedSecretException(
            "Error computing shared secret (status code 10) due to EDeviceKey.Pub with incompatible curve: $curve"
        )

    class MalformedKey(cause: Throwable) :
        SharedSecretException(
            "Error computing shared secret (status code 10) due to malformed EDeviceKey.Pub",
            cause
        )
}
