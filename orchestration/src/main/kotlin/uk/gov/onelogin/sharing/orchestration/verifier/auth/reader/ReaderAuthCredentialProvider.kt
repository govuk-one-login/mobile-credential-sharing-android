package uk.gov.onelogin.sharing.orchestration.verifier.auth.reader

/**
 * Functional interface for creating COSE_Sign1 signatures.
 */
fun interface ReaderAuthCredentialProvider {
    /**
     * @param readerAuthenticationPayload The [ByteArray] structure matching Reader Authentication.
     *
     * @return A [ByteArray] representing a `COSE_Sign1` data structure.
     *
     * @throws uk.gov.onelogin.sharing.orchestration.exceptions.RecoverableError when the User can
     * reattempt the action
     * @throws uk.gov.onelogin.sharing.orchestration.exceptions.UnrecoverableError when the journey
     * should finish / complete.
     */
    fun sign(readerAuthenticationPayload: ByteArray): ByteArray

    /**
     * Functional interface for generating instances of [ReaderAuthCredentialProvider].
     */
    fun interface Factory {

        /**
         * @return A new instance of [ReaderAuthCredentialProvider].
         */
        fun create(): ReaderAuthCredentialProvider
    }
}
