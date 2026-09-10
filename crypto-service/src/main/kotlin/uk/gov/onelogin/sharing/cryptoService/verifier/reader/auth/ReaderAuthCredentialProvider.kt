package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

/**
 * Functional interface for creating COSE_Sign1 signatures.
 */
fun interface ReaderAuthCredentialProvider {
    /**
     * @param readerAuthenticationPayload The [ByteArray] structure matching Reader Authentication.
     *
     * @return A [ByteArray] representing a `COSE_Sign1` data structure.
     *
     * @throws uk.gov.onelogin.sharing.cryptoService.verifier.ReaderAuthenticationException when
     * the `COSE_Sign1` signature cannot be created.
     */
    suspend fun sign(readerAuthenticationPayload: ByteArray): ByteArray

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
